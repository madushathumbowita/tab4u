package io.tab4u.tab4u.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.tab4u.tab4u.dto.TablatureResponse;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.*;


@Service
public class TablatureService {

    public TablatureResponse buildResponse(JSONArray jsonArray){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(String.valueOf(jsonArray));
            if(jsonNode.isArray()){
                int length = jsonNode.size();

                String[][] tablature = new String[7][length];
                for (int i = 0; i < 7; i++) {
                    for (int j = 0; j < length; j++) {
                        tablature[i][j] = "";
                    }
                }

                int i = 0;
                for (JsonNode element : jsonNode) {
                    String stringNumber = element.get("stringNo").asText();
                    tablature[Integer.parseInt(stringNumber)][i] = element.get("fret").asText();
                    tablature[6][i] = element.get("finger").asText();
                    i++;
                }
                return new TablatureResponse(jsonArray.toString(), tablature);
            }
        }
        catch (Exception exception){
            System.out.println(exception.getLocalizedMessage());
        }
        return null;
    }

    public AudioInputStream convertToWav(MultipartFile file) {
        try {
            ByteArrayOutputStream pcmOutputStream = new ByteArrayOutputStream();
            InputStream inputStream = file.getInputStream();
            Bitstream bitstream = new Bitstream(inputStream);
            Decoder decoder = new Decoder();

            while (true) {
                Header frameHeader = bitstream.readFrame();
                if (frameHeader == null) {
                    break;
                }
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);
                short[] pcm = output.getBuffer();
                for (short s : pcm) {
                    pcmOutputStream.write((s & 0xFF));
                    pcmOutputStream.write((s >> 8) & 0xFF);
                }
                bitstream.closeFrame();
            }

            // Step 2: Write PCM data to WAV format
            byte[] pcmBytes = pcmOutputStream.toByteArray();
            ByteArrayInputStream pcmInputStream = new ByteArrayInputStream(pcmBytes);

            AudioFormat audioFormat = new AudioFormat(
                    44100, 16, 2, true, false);
            return new AudioInputStream(pcmInputStream, audioFormat, pcmBytes.length / audioFormat.getFrameSize());
        }
        catch (Exception exception){
            return null;
        }
    }
}
