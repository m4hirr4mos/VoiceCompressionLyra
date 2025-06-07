# Voice Compression using Lyra
Real-time underwater voice communication using Lyra codec and UnetStack simulator. Audio is recorded via a web page, compressed with a custom Groovy agent, transmitted between nodes, then decoded and played on a second web page. Enables low-bitrate voice streaming in underwater networks. The project was execute on a Linux server.




Project Components:
1. LyraProcessor.groovy (Groovy Agent)
A custom fjåge agent that handles:

-Encoding recorded WAV audio to .lyra format

-Decoding received .lyra audio back to WAV

-Handling DatagramReq and DatagramNtf messages in UnetStack

3. Browser 1 (Webpage to Record & Send Audio):
   
-Records audio via microphone (MediaRecorder API)

-Converts audio to WAV format and sends it using a DatagramReq to the agent

-Served from Node B

5. Browser 2 (Webpage to Receive & Play Audio):
   
-Subscribes to agent messages using unet.js

-Listens for DatagramNtf containing audio data

-Plays decoded audio using AudioContext

-Served from Node A

Technologies Used:

1.UnetStack Simulator (Underwater network simulation)

2.Lyra V2 Codec (Google’s low-bitrate audio codec)

3.Groovy & fjåge (for agent behavior and messaging)

4.JavaScript & HTML (for recording, UI, and playback)

5.FFmpeg (for WAV conversion)

System Workflow:
1.User records audio on Browser 1 (Node B).

2.Audio is sent to the LyraProcessor agent via DatagramReq.

3.Agent encodes the audio using Lyra and sends it across to Node A.

4.The receiving LyraProcessor agent decodes it.

5.Browser 2 (Node A) receives the audio and plays it back.


Setup Instructions:
1. Build Lyra Codec
   
Follow instructions in Lyra GitHub and install:

Bazel 5+

Python 3, NumPy

Then run:
bazel build -c opt lyra/cli_example:encoder_main
bazel build -c opt lyra/cli_example:decoder_main

3. Setup UnetStack Simulator:
-Download UnetStack Community:
https://unetstack.net/#downloads

-Run:
bin/unet samples/2-node-network.groovy

-Open:
Node A: http://localhost:8081/
Node B: http://localhost:8082/

3. Serve HTML Pages
Use a static server (e.g., http-server) to serve Browser 1 on Node B and Browser 2 on Node A.

4. ✅ Features:
-Low-bitrate voice compression with Lyra
-Real-time audio transmission over simulated underwater links
-Full-stack integration (Web ↔ Agent ↔ UnetStack)

5. 📢 Credits:
-UnetStack – ARL, National University of Singapore
-Lyra Codec – Google







