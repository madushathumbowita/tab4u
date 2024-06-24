import React from "react";
import image from "./../static/images/image4.png";

export default function ApplicationContent() {
  return (
    <div className="landing-page">
      <div className="text-center my-9">
        <p className="text-2xl sm:text-4xl md:text-5xl lg:text-6xl font-semibold text-gray-800 my-5">
          Welcome to Tab4U
        </p>
        <p className="text-md sm:text-xl md:text-2xl lg:text-3xl font-light text-gray-600">
          Generate a Guitar Tablature from an audio
        </p>
      </div>
      <div className="flex flex-col lg:flex-row bg-purpleBackground p-5 lg:p-10 justify-center items-center">
        <div className="shadow-2xl h-1/3 lg:w-1/3 rounded-2xl">
          <img src={image} className="rounded-xl mx-auto lg:ml-0" />
        </div>
        <div className="text-center tracking-wider leading-loose ml-0 lg:ml-20 text-xs sm:text-sm md:text-lg lg:text-2xl mt-20 lg:mt-0 lg:w-1/4 text-gray-800">
          " Tab4U is a web-based platform that generates guitar tablature from a
          given audio file, including finger placement positions. It takes an
          audio file as input and provides the corresponding guitar tablature
          with detailed finger positions to the user. "
        </div>
      </div>
    </div>
  );
}
