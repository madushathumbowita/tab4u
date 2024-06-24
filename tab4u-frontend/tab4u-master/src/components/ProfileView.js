import React, { useState } from 'react';
import PersonIcon from '@mui/icons-material/Person';
import { auth } from './../Firebase-config';

export default function ProfileView() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const handleLogout = () => {
    auth.signOut();
  }

  return (
    <div className="relative">
      <button onClick={toggleMenu}>
        <PersonIcon fontSize='large' color="disabled"/>
      </button>
      {isMenuOpen && (
        <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg">
          {/* <button className="block px-4 py-2 w-full text-gray-800 hover:bg-gray-200">Profile</button> */}
          <button onClick={handleLogout} className="block px-4 py-2 w-full text-gray-800 hover:bg-gray-200">Logout</button>
        </div>
      )}
    </div>
  );
}
