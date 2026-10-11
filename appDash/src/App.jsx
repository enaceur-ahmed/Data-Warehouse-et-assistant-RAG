/* Point d'entrée : routage (React Router) */

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import PageOne from './pages/PageOne';
import PageTwo from './pages/PageTwo';
import PageThree from './pages/PageThree';
import PageFour from './pages/PageFour';
import Assistant from './pages/Assistant';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/page1" replace />} />
        <Route element={<Layout />}>
          <Route path="/page1" element={<PageOne />} />
          <Route path="/page2" element={<PageTwo />} />
          <Route path="/page3" element={<PageThree />} />
          <Route path="/page4" element={<PageFour />} />
          <Route path="/assistant" element={<Assistant />} />
        </Route>
        <Route path="*" element={<Navigate to="/page1" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
