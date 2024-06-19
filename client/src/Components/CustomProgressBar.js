import React, { useState, useEffect } from 'react';
import ProgressBar from 'react-bootstrap/ProgressBar';
import 'bootstrap/dist/css/bootstrap.min.css';

const ProgressComponent = ({ uploading, responseReceived }) => {
  const [currentProgress, setCurrentProgress] = useState(0);

  useEffect(() => {
    let progressInterval;
    if (uploading) {
      progressInterval = setInterval(() => {
        setCurrentProgress(prevProgress => {
          if (prevProgress < 100) {
            return prevProgress + 5;
          } else {
            clearInterval(progressInterval);
            return 100;
          }
        });
      }, 100);
    }

    return () => clearInterval(progressInterval);
  }, [uploading]);

  useEffect(() => {
    if (responseReceived) {
      const animationDuration = 1000; 
      const animationSteps = 20;
      const step = 100 / animationSteps;
      let currentStep = 0;

      const animationInterval = setInterval(() => {
        setCurrentProgress(prevProgress => {
          if (currentStep < animationSteps) {
            currentStep++;
            return prevProgress + step;
          } else {
            clearInterval(animationInterval);
            return 100;
          }
        });
      }, animationDuration / animationSteps);

      return () => clearInterval(animationInterval);
    }
  }, [responseReceived]);

  const progressBarStyle = {
    width: '400px' // Change the width 
  };

  return (
    <div>
      {currentProgress === 100 && responseReceived && (
        <center>
          <h5>File uploaded successfully!</h5>
        </center>
      )}
      <center>
        <ProgressBar now={currentProgress} label={`${currentProgress}%`} style={progressBarStyle} />
      </center>
    </div>
  );
};

export default ProgressComponent;
