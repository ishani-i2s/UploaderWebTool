import React from "react";
import BannerBackground from "../Assets/home-banner-background.png";
import BannerImage from "../Assets/home-banner-image.png";
import Navbar from "../Components/Navbar";
import { FiArrowRight } from "react-icons/fi";
import {useNavigate} from 'react-router-dom';
import OAuth2Login from 'react-simple-oauth2-login';
import { useState } from 'react';

const Home = () => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const[state, setState] = useState('');
    const[code, setCode] = useState('');
    const[accessToken, setAccessToken] = useState('');
    const navigate = useNavigate();

    // Function to handle successful login
    const handleSuccess = (response) => {
        console.log('Login successful:', response);
        setIsLoggedIn(true);
        setCode(response.code);
        setState(response.state);
        // You can perform additional actions after successful login, like redirecting to another page.
    };

    // Function to handle failed login
    const handleFailure = (response) => {
        console.error('Login failed:', response);
        // Handle login failure here, e.g., show an error message to the user.
    };

    const generateState = () => {
        return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    }

    const config = {
        authorizationUrl:"https://ifscloud.tsunamit.com/auth/realms/tsutst/protocol/openid-connect/auth",
        clientId:"I2S_UPLOADER",
        redirectUri:"http://localhost:3000/",
        responseType:"code",
        scope:"openid microprofile-jwt",
        state:generateState(),
        onSuccess: handleSuccess,
        onFailure: handleFailure,
    };

    const retreiveAccessToken = () => {
        console.log('Code:', code);
        console.log('State:', state);
        //clear the local storage
        localStorage.clear();

        var formBody = [];
        var details = {
          'code': code,
          'state': state,
          'client_id': 'I2S_UPLOADER',
          'grant_type': 'authorization_code',
          'redirect_uri': 'http://localhost:3000/',
        };
        for (var property in details) {
          var encodedKey = encodeURIComponent(property);
          var encodedValue = encodeURIComponent(details[property]);
          formBody.push(encodedKey + "=" + encodedValue);
        }
        formBody = formBody.join("&");
  
        fetch('/auth/realms/tsutst/protocol/openid-connect/token', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
            'Access-Control-Allow-Origin': '*'
          },
          body: formBody
        }).then(response => {
          var data = response.json();
          Promise.resolve(data).then(function(value) {
            console.log(value);
            if(value.access_token !== undefined){
              setAccessToken(value.access_token);
              //set the access token in local storage
              //retreive method -: const accessToken = localStorage.getItem('accessToken');
              localStorage.setItem('accessToken', value.access_token);
              localStorage.setItem('refreshToken', value.refresh_token);
              localStorage.setItem('idToken', value.id_token);
              localStorage.setItem('login', true);
            }
            console.log(value.access_token);
            navigate('/home');
          });
        }).catch(error => {
          console.error('There was a problem with your fetching the access token:', error);
        });
    };
    
    return (
      <div className="home-container">
        <Navbar />
        <div className="home-banner-container">
          <div className="home-bannerImage-container">
            <img src={BannerBackground} alt="" />
          </div>
          <div className="home-text-section">
            <h1 className="primary-heading">
              Uploading your <br /> files is easy
            </h1>
            <p className="primary-text">
                Upload your details to IFS with ease. <br /> Get started now.
            </p>
            {isLoggedIn ? (
              retreiveAccessToken()
            ) : (
                <OAuth2Login {...config} className="secondary-button">
                    Login
                    <FiArrowRight />{" "}
                </OAuth2Login>
            )}
          </div>
          <div className="home-image-section">
            <img className="homebannar" src={BannerImage} alt="" />
          </div>
        </div>
      </div>
    );
};

export default Home;