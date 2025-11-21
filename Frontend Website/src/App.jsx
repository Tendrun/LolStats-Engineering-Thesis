import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar/Navbar.jsx";
import "./App.css";
import Champions from "./pages/ChampionsList/ChampionsList.jsx";
import ChampionDashboard from "./pages/ChampionDashboard/ChampionDashboard";
import {
  QueryClient,
  QueryClientProvider,
} from '@tanstack/react-query';
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'


const queryClient = new QueryClient()

function App() {
  return (
  <QueryClientProvider client={queryClient}>
    <Router>
    <Navbar />
    <div className="main-content">
      <div className="content-container">
        <Routes>
          <Route path="/Champions" element={<Champions />} />
          <Route path="/ChampionDashboard" element={<ChampionDashboard />} />
        </Routes>
      </div>
    </div>
    </Router>
</QueryClientProvider>

  );
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>
)

export default App