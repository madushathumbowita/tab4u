package io.tab4u.tab4u.controller;

import io.tab4u.tab4u.dto.TablatureResponse;
import io.tab4u.tab4u.generators.TabFingerGeneration;
import io.tab4u.tab4u.service.TablatureService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api")
public class TablatureController {

    @Autowired
    TabFingerGeneration tabFingerGeneration;

    @Autowired
    TablatureService tablatureService;

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<?> generate(@RequestParam("audioFile") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("File Not Found", HttpStatus.BAD_REQUEST);
        }
        try {
            AudioInputStream audioInputStream;
            // If the file type is not wav ex: mp3
            if(!Objects.equals(file.getContentType(), "audio/wav") && !Objects.equals(file.getContentType(), "audio/wave")){
                audioInputStream = tablatureService.convertToWav(file);
            }
            else {
                InputStream inputStream = file.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
            }
            JSONArray results = tabFingerGeneration.generateTablature(audioInputStream);
            TablatureResponse response = tablatureService.buildResponse(results);
            if(response == null){
                return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException | UnsupportedAudioFileException e) {
            System.out.println(e.getLocalizedMessage());
            return new ResponseEntity<>("Failed to process file", HttpStatus.BAD_REQUEST);
        }
    }
}
