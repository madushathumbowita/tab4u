import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
    apiKey: "AIzaSyDwOaypXWaHkxtBh7KeDHMmUZhpSC0Bu9w",
    authDomain: "fyptab4u.firebaseapp.com",
    projectId: "fyptab4u",
    storageBucket: "fyptab4u.appspot.com",
    messagingSenderId: "355093877347",
    appId: "1:355093877347:web:286af87735751afa4f5e56",
    measurementId: "G-2YPDCZ0K1D"
};

const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);