import React, { useState } from 'react';
import SignIn from './SignIn';
import SignUp from './SignUp';
import { Typography } from '@mui/material';

export default function AuthenticationContainer() {
  const [loginView, setLoginView] = useState(true);

  return (
    <div className="
        flex 
        xl:items-center 
        justify-center 
        w-full
        
        xl:m-0
      ">
      <div className="bg-white rounded-xl shadow-2xl xl:w-4/5 w-full xl:h-4/5 mx-7 xl:mx-0 relative p-16 xl:mb-0 mb-10">
        <div className="text-center">
          <Typography style={{
            fontSize: "2.2vw",
            fontFamily: "Arial",
            marginBottom: "10px"

          }}>
            Welcome !
          </Typography>
          <Typography
            style={{
              fontSize: "1.1vw",
              fontFamily: "calibri",
              marginBottom: "10px"
            }}>
            Enter your details
          </Typography>
        </div>

        <div className=" w-full bg-gray-200 my-5 rounded-full">
          <div className="bg-gray-300 flex justify-around p-1 rounded-full">
            <button
              onClick={() => setLoginView(true)}
              className={`w-1/2 rounded-full px-4 py-2 text-2xl ${loginView ? 'bg-white' : 'bg-gray-300'}`}>
              Sign in
            </button>
            <button
              onClick={() => setLoginView(false)}
              className={`w-1/2 rounded-full px-4 py-2 text-2xl ${!loginView ? 'bg-white' : 'bg-gray-300'}`}>
              Sign up
            </button>
          </div>
        </div>

        {loginView ? <SignIn /> : <SignUp />}

      </div>
    </div>
  );
}
