import React from 'react'
import NavBar from '../components/NavBar'
import UserInput from '../components/UserInput'
import ApplicationContent from '../components/ApplicationContent'

export default function HomePage() {
  return (
    <div>
      <NavBar />
      <ApplicationContent />
      <UserInput />
    </div>
  )
}
