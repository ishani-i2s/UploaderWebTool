import React, { useEffect } from 'react';
import {useNavigate} from 'react-router-dom';
import Navbar from "../Components/Navbar";

export default function Home() {
    const navigate = useNavigate();
    const navigateToFO = () => {
        // 👇️ Navigate to /contacts
        navigate('/fileUpload');
      };

    const navigateToFA = () => {
        // 👇️ Navigate to /contacts
        navigate('/fixedAssets');
      };

    const navigateToFOD = () => {
        // 👇️ Navigate to /contacts
        navigate('/fileDownload');
      }

    const navigateToRC = () => {
        // 👇️ Navigate to /contacts
        navigate('/routeChanges');
      }

    const navigateToUTD = () => {
        // 👇️ Navigate to /contacts
        navigate('/taskDetailsDownload');
      }

    //if the access token is not present, redirect to the login page
    useEffect(() => {
        const accessToken = localStorage.getItem('accessToken');
        if (!accessToken) {
          navigate('/');
        }
    }, []);

    
    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
          {/* Top Navigation Bar */}
          <Navbar />
    
        {/* Main Content */}
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', backgroundColor: '#ffffff', padding: '50px' }}>
        <h1 style={{ marginBottom: '40px', color: '#333', fontSize: '2rem', fontWeight: 'bold', textAlign: 'center' }}>Welcome to UPLOADER TOOL</h1>
        {/* Your buttons */}
        <div style={{ display: 'flex', justifyContent: 'center' }}>
            <button style={{ padding: '12px 24px', margin: '0 10px', fontSize: '1rem', borderRadius: '8px', backgroundColor: '#007bff', color: '#fff', border: 'none', cursor: 'pointer', boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)', transition: 'background-color 0.3s' }} onClick={navigateToFO}>Functional Object Upload</button>
            <button style={{ padding: '12px 24px', margin: '0 10px', fontSize: '1rem', borderRadius: '8px', backgroundColor: '#ffa500', color: '#fff', border: 'none', cursor: 'pointer', boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)', transition: 'background-color 0.3s' }} onClick={navigateToFOD}>Functional Object Download</button>
            <button style={{ padding: '12px 24px', margin: '0 10px', fontSize: '1rem', borderRadius: '8px', backgroundColor: '#A569BD', color: '#fff', border: 'none', cursor: 'pointer', boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)', transition: 'background-color 0.3s' }} onClick={navigateToRC}>Download Task Details</button>
            <button style={{ padding: '12px 24px', margin: '0 10px', fontSize: '1rem', borderRadius: '8px', backgroundColor: '#CA6F1E', color: '#fff', border: 'none', cursor: 'pointer', boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)', transition: 'background-color 0.3s' }} onClick={navigateToUTD}>Update Task Details</button>
            <button style={{ padding: '12px 24px', margin: '0 10px', fontSize: '1rem', borderRadius: '8px', backgroundColor: '#28a745', color: '#fff', border: 'none', cursor: 'pointer', boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)', transition: 'background-color 0.3s' }} onClick={navigateToFA}>Fixed Assets</button>
        </div>
        </div>
    
          {/* Footer */}
          <div style={{ backgroundColor: '#333', color: 'white', padding: '10px', textAlign: 'center' }}>
            {/* Your footer content */}
            <p>© 2024 My Website. All rights reserved.</p>
          </div>
        </div>
      );
}