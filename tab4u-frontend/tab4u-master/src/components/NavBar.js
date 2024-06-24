import React from "react";
import Logo from "./Logo";
import ProfileView from "./ProfileView";
import image from './../static/images/logo 5.png'

function NavBar() {
  return (
    <div className="w-full text-black flex items-center justify-between px-10 py-2 mt-2">
      {/* <Logo /> */}
      <img src={image} width={100}/>
      <ProfileView />
    </div>
  );
}

export default NavBar;
