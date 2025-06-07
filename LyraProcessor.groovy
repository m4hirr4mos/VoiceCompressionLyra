import org.arl.fjage.*
import org.arl.unet.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LyraProcessor extends UnetAgent {

    @Override
    void startup() {
        subscribeForService(Services.DATAGRAM)
        subscribeForService(Services.LINK)
    }

    static String getFileType(String filePath) {
        return filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase()
    }

    static def lyraEncoderBin = "/home/m4hirr4mos/lyra/bazel-bin/lyra/cli_example/encoder_main"
    static def lyraDecoderBin = "/home/m4hirr4mos/lyra/bazel-bin/lyra/cli_example/decoder_main"
    static def ffmpegBin = "/snap/bin/ffmpeg" // Path to ffmpeg binary

    static Path convertToValidWav(Path inputPath) {
        Path fixedWavPath = Paths.get("/home/m4hirr4mos/temp/fixed_file.wav")
        def command = [
            ffmpegBin, "-y", 
            "-i", inputPath.toString(), 
            "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1", 
            fixedWavPath.toString()
        ] as String[]

        println("Converting audio to valid WAV format with command: ${command.join(' ')}")
        ProcessBuilder builder = new ProcessBuilder(command)
        builder.inheritIO()
        Process process = builder.start()
        process.waitFor()

        if (process.exitValue() != 0) {
            throw new RuntimeException("Audio conversion failed with exit code: ${process.exitValue()}")
        }

        println("Conversion successful: ${fixedWavPath}")
        return fixedWavPath
    }

    static Path encodeWithLyraEncoder(Path inputPath, Path outputDir) {
        def bitrate = 3200
        def outputPath = Paths.get("/home/m4hirr4mos/temp/temp_file.lyra")
        def command = [
            lyraEncoderBin,
            "--input_path=${inputPath.toString()}",
            "--output_dir=${outputDir.toString()}",
            "--bitrate=${bitrate}"
        ] as String[]
        println("Running Lyra encoder with command: ${command.join(' ')}")
        ProcessBuilder builder = new ProcessBuilder(command)
        builder.inheritIO()
        Process process = builder.start()
        process.waitFor()
        if (process.exitValue() != 0) {
            throw new RuntimeException("Encoding failed with exit code: ${process.exitValue()}")
        }
        println("Encoding completed")
        return outputPath
    }

    static Path decodeWithLyraDecoder(Path encodedPath, Path outputDir) {
        def outputPath = Paths.get("/home/m4hirr4mos/temp/temp_file_decoded.wav")
        def command = [
            lyraDecoderBin,
            "--encoded_path=${encodedPath.toString()}",
            "--output_dir=${outputDir.toString()}",
            "--bitrate=3200"
        ] as String[]
        println("Running Lyra decoder with command: ${command.join(' ')}")
        ProcessBuilder builder = new ProcessBuilder(command)
        builder.inheritIO()
        Process process = builder.start()
        process.waitFor()
        if (process.exitValue() != 0) {
            throw new RuntimeException("Decoding failed with exit code: ${process.exitValue()}")
        }
        println("Decoding completed")
        return outputPath
    }

    static Path processFile(Path filePath, Path outputDir, boolean isEncoding) {
        return isEncoding ? encodeWithLyraEncoder(filePath, outputDir) : decodeWithLyraDecoder(filePath, outputDir)
    }

    Message processRequest(Message msg) {
        if (msg instanceof DatagramReq && msg.protocol == Protocol.USER) {
            Path tempFilePath = Paths.get(System.getenv("HOME"), "temp", "temp_file.wav")
            Files.write(tempFilePath, msg.data)

            def uwlink = agentForService(org.arl.unet.Services.LINK)
            Path validWavFile = convertToValidWav(tempFilePath) // Convert before encoding
            
            Path encodedFilePath = processFile(validWavFile, Paths.get(System.getenv("HOME"), "temp"), true)
            byte[] encodedData = Files.readAllBytes(Paths.get("/home/m4hirr4mos/temp/fixed_file.lyra"))

            send new DatagramReq(
                recipient: uwlink,
                to: msg.to,
                protocol: Protocol.USER,
                data: encodedData
            )

            return new Message(msg, Performative.AGREE)
        }
    }

    @Override
    void processMessage(Message msg) {
        if (msg instanceof DatagramNtf && msg.protocol == Protocol.USER) {
            Path tempFilePath = Paths.get(System.getenv("HOME"), "temp", "temp_file.lyra")
            Files.write(tempFilePath, msg.data)

            Path decodedFilePath = processFile(tempFilePath,Paths.get(System.getenv("HOME"), "temp"), false);

            byte[] decodedData = Files.readAllBytes(decodedFilePath)

            send new DatagramNtf(
                recipient: topic(),
                protocol: Protocol.DATA,
                data: decodedData
            )
        }
    }
}


