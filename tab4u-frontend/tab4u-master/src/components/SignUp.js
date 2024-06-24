import React, { useState } from 'react';
import app from '../Firebase-config'
import { createUserWithEmailAndPassword } from "firebase/auth";
import { auth } from './../Firebase-config';
import Popup from './Popup';
import { useNavigate } from 'react-router-dom';

export default function SignUp() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const navigate = useNavigate();

  const handleEmailChange = (e) => {
    setEmail(e.target.value);
  };

  const handlePasswordChange = (e) => {
    setPassword(e.target.value);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      await createUserWithEmailAndPassword(auth, email, password);
      const user = auth.currentUser;
      if (user) {
        const idTokenResult = await user.getIdTokenResult();
        const expirationTime = new Date(idTokenResult.expirationTime).getTime();
        const timeoutDuration = expirationTime - Date.now();
        navigate("/home")

        setTimeout(() => {
          auth.signOut();
        }, timeoutDuration);
      }
    } catch (error) {
      const { message } = error
      const [head, ...rest] = message.split(':');
      const secondPart = rest.join(':');
      setErrorMessage(secondPart)
    }
  }

  return (
    <div className="flex items-center justify-center">
      <Popup error={errorMessage} setErrorMessage={setErrorMessage} /> 
      <form className="w-full">
        <div className="flex flex-col h-full">
          <div className="mb-4 flex-grow">
            <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="email">
              Email
            </label>
            <input
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline h-full"
              id="email"
              type="email"
              placeholder="Email"
              value={email}
              onChange={handleEmailChange}
            />
          </div>
          <div className="mb-6 flex-grow">
            <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="password">
              Password
            </label>
            <input
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:shadow-outline h-full"
              id="password"
              type="password"
              placeholder="Password"
              value={password}
              onChange={handlePasswordChange}
            />
          </div>
          <div className="flex items-center justify-center">
            <button
              className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
              type="button"
              onClick={handleSubmit}
            >
              Sign Up
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
