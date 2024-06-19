// import './App.css';
// import './index.css';
// import FileUpload from './pages/FileUpload';
// import LoginCard from './pages/Login';

// function App() {
//   return (
//     <div className="App">
//       {/* <FileUpload /> */}
//       <LoginCard />
//     </div>
//   );
// }

// export default App;



import './App.css';
import './index.css';
import { Routes, Route, BrowserRouter } from 'react-router-dom';
import FileUpload from './pages/FileUpload';
import FODownload from './pages/FunctionalObjectDownload';
import Home from './pages/Home';
import FixedAssets from './pages/FixedAssets';
import Landing from './pages/landing';
import RouteChanges from './pages/RouteChanges';
import TaskDetailsDownload from './pages/UpdateTaskDetails';

export default function App() {
  return (
    <div>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Landing/>} />
          <Route path="/home" element={<Home />} />
          <Route path="/fileUpload" element={<FileUpload />} />
          <Route path="/fixedAssets" element={<FixedAssets />} />
          <Route path="/fileDownload" element={<FODownload />} />
          <Route path="/routeChanges" element={<RouteChanges/>} />
          <Route path="/taskDetailsDownload" element={<TaskDetailsDownload/>} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

