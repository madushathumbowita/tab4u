import React, { useRef, useState } from 'react';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import MicIcon from '@mui/icons-material/Mic';
import PauseCircleIcon from '@mui/icons-material/PauseCircle';
import PlayCircleIcon from '@mui/icons-material/PlayCircle';
import VolumeDownIcon from '@mui/icons-material/VolumeDownRounded';
import VolumeUpIcon from '@mui/icons-material/VolumeUpRounded';
import DownloadIcon from '@mui/icons-material/Download';
import { Slider, Typography } from '@mui/material';
import { useMediaQuery } from 'react-responsive';
import { generateTablature } from '../services/apiRequest';
import Output from './Output';
import handImage from './../static/images/hand-finger-numbers.png'

export default function UserInput() {
    const [selectedFile, setSelectedFile] = useState(null);
    const [inputName, setInputName] = useState("xxxxxxxxx");
    const [inputPlaying, setInputPlaying] = useState(false);
    const [isRecording, setIsRecording] = useState(false);
    const [currentTime, setCurrentTime] = useState(0);
    const [duration, setDuration] = useState(0);
    const [volume, setVolume] = useState(1);
    const [recordedAudio, setRecordedAudio] = useState(null);
    const [results, setResults] = useState(null);

    const audioRef = useRef(null);
    const mediaRecorderRef = useRef(null);
    const chunks = useRef([]);

    const toggleMusicPlaying = () => {
        setInputPlaying(!inputPlaying);
    }

    const handlePlayPause = () => {
        if (inputPlaying) {
            audioRef.current.pause();
        } else {
            audioRef.current.play();
        }
        toggleMusicPlaying();
    };

    const handleTimeUpdate = () => {
        setCurrentTime(audioRef.current.currentTime);
        setDuration(audioRef.current.duration);
    };

    const handleSeek = (e, newValue) => {
        audioRef.current.currentTime = newValue;
        setCurrentTime(newValue);
    };

    const handleVolumeChange = (e, newValue) => {
        setVolume(newValue);
        audioRef.current.volume = newValue;
    };

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        setSelectedFile(file);
        setInputName(file.name);
        const url = URL.createObjectURL(file);
        audioRef.current.src = url;
        audioRef.current.play();
        setInputPlaying(true);
    };

    const handleStartRecording = async () => {
        if (!isRecording) {
            setIsRecording(true);
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            mediaRecorderRef.current = new MediaRecorder(stream);
            mediaRecorderRef.current.ondataavailable = (event) => {
                if (event.data.size > 0) {
                    chunks.current.push(event.data);
                }
            };
            mediaRecorderRef.current.onstop = () => {
                const blob = new Blob(chunks.current, { type: 'audio/wav' });
                const url = URL.createObjectURL(blob);
                setRecordedAudio(url);
                setInputName('Recorded Audio');
                audioRef.current.src = url;
                chunks.current = [];
            };
            mediaRecorderRef.current.start();
        } else {
            mediaRecorderRef.current.stop();
            setIsRecording(false);
        }
    };

    const isSmallScreen = useMediaQuery({ query: '(max-width: 640px)' });
    const isMediumScreen = useMediaQuery({ query: '(max-width: 1024px)' });

    let sliderWidth = '300px';

    if (isMediumScreen) {
        sliderWidth = '200px';
    }
    if (isSmallScreen) {
        sliderWidth = '150px';
    }

    const handleGenerateTabs = async () => {
        const output = await generateTablature(selectedFile);
        if (output != null) {
            const { tablature } = output
            setResults(tablature)
        }
    }

    const svgRef = useRef(null);

    const handleDownloadClick = () => {
        if (svgRef.current) {
            const serializer = new XMLSerializer();
            const svgString = serializer.serializeToString(svgRef.current);
    
            const canvas = document.createElement("canvas");
            const context = canvas.getContext("2d");
    
            const image = new Image();
            image.onload = () => {
                const svgWidth = svgRef.current.getBoundingClientRect().width;
                const svgHeight = svgRef.current.getBoundingClientRect().height;
                canvas.width = svgWidth;
                canvas.height = svgHeight;
    
                context.fillStyle = "#FFFFFF";
                context.fillRect(0, 0, svgWidth, svgHeight);
                context.drawImage(image, 0, 0, svgWidth, svgHeight);
    
                const jpgUrl = canvas.toDataURL("image/jpeg");
                const downloadLink = document.createElement("a");
                downloadLink.href = jpgUrl;
                downloadLink.download = "my-image-file.jpg";
                document.body.appendChild(downloadLink);
                downloadLink.click();
                document.body.removeChild(downloadLink);
            };
            const svgBlob = new Blob([svgString], { type: "image/svg+xml" });
            const svgUrl = URL.createObjectURL(svgBlob);
            image.src = svgUrl;
        }
    };
    

    return (
        <div>
            {/* Input and Preview Container */}
            <div className="w-full bg-gray-300 py-4">

                {/* The Input Selection Buttons for the user input */}
                <div className="flex flex-col justify-center items-center py-9 sm:flex-row sm:justify-center">
                    <div className='w-80 my-2'>
                        <input
                            type="file"
                            accept="audio/*"
                            onChange={handleFileChange}
                            style={{ display: 'none' }}
                        />
                        <label className="flex px-16 py-3 justify-center items-center border-[2px] border-purple-700 border-solid text-purple-500 font-bold hover:bg-purple-500 hover:text-white sm:rounded-l-lg ">
                            <button onClick={() => { document.querySelector("input[type='file']").click() }} className="flex justify-center items-center">
                                <span className="mr-2 text-lg">Select File</span>
                                <CloudUploadIcon fontSize='large' />
                            </button>
                        </label>

                    </div>

                    <div className="w-80 my-2">
                        <button
                            className={`w-full flex px-16 py-3 justify-center items-center border-[2px] sm:border-l-0 sm:rounded-r-lg border-purple-700 border-solid text-purple-500 font-bold hover:bg-purple-500 hover:text-white ${isRecording ? 'bg-red-500 text-white' : ''}`}
                            onClick={handleStartRecording}
                        >
                            <span className="mr-2 text-lg">{isRecording ? 'Stop Recording' : 'Record'}</span>
                            <MicIcon fontSize='large' />
                        </button>
                    </div>
                </div>



                {/* Preview section of the input item */}
                <div>
                    <div className="flex justify-center items-center mt-10 mb-9">
                        <Typography style={{ fontFamily: 'Arial', fontWeight: 'bold' }}> Playing : {inputName} </Typography>
                    </div>

                    {/* Control the input section */}
                    <div className="flex justify-center items-center h-12">
                        <div className="flex items-center">
                            <div className="mr-8">
                                {inputPlaying ?
                                    <button onClick={handlePlayPause} >
                                        <PauseCircleIcon fontSize='large' />
                                    </button>
                                    :
                                    <button onClick={handlePlayPause} >
                                        <PlayCircleIcon fontSize='large' />
                                    </button>
                                }
                            </div>
                            <audio
                                ref={audioRef}
                                src=""
                                onTimeUpdate={handleTimeUpdate}
                                onLoadedMetadata={handleTimeUpdate}
                                volume={volume}
                            />
                            <Slider
                                size="large"
                                value={currentTime}
                                max={duration || 0}
                                onChange={handleSeek}
                                style={{ width: sliderWidth, color: '#bc62fc' }}
                            />
                        </div>
                    </div>
                    {/* Custom Volume control h-12 mr-40 -mt-12 */}
                    <div className="flex items-center justify-center lg:justify-end lg:h-12 lg:mr-40 lg:-mt-12">
                        <VolumeDownIcon />
                        <Slider
                            size='small'
                            value={volume}
                            min={0}
                            max={1}
                            step={0.01}
                            onChange={handleVolumeChange}
                            style={{ width: '100px', marginLeft: '8px', marginRight: '8px', color: '#bc62fc' }}
                        />
                        <VolumeUpIcon />
                    </div>
                </div>
            </div>

            {/* Generating tabs sections */}
            <div className="py-9">
                <div className="flex justify-center">
                    <button className='w-50 h-18 text-white px-10 py-5 rounded-xl sm:mb-0 mb-4 bg-purple-500 disabled:bg-gray-400' onClick={handleGenerateTabs} disabled={selectedFile == null} >
                        <span style={{ fontFamily: 'Arial', fontWeight: 'bold', textAlign: 'center' }}> Generate Tabs </span>
                    </button>
                </div>
                <div className="flex justify-center sm:ml-80 sm:-mt-12 mb-4">
                    <button className='w-50 h-18 text-purple-500 disabled:text-gray-400 mx-10' onClick={handleDownloadClick} disabled={selectedFile == null} >
                        <DownloadIcon />
                    </button>
                </div>
            </div>

            <div className="w-full flex justify-center items-center mt-10 mb-24">
                {results && (
                    <div className="w-4/5 flex justify-center items-center">
                        <Output data={results} ref={svgRef} />
                        <img src={handImage} width={150} />
                    </div>
                )}
            </div>

            <div className='bg-purple-500 w-full h-2' />

        </div>
    );
}
