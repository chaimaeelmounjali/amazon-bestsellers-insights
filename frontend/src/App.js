import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ExplorationPage from './pages/ExplorationPage';
import BestSellersPage from './pages/BestSellersPage';
import ProtectedRoute from './components/ProtectedRoute';
import InventoryPage from './pages/seller/InventoryPage';
import ProductFormPage from './pages/seller/ProductFormPage';
import PredictionPage from './pages/seller/PredictionPage';
import CreateSalePage from './pages/seller/CreateSalePage';
import ProductDetailPage from './pages/ProductDetailPage';
import UserManagementPage from './pages/admin/UserManagementPage';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Navbar />
          <div className="content-container">
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/login" element={<LoginPage />} />
              {/* Route protégée pour le dashboard */}
              <Route
                path="/dashboard"
                element={
                  <ProtectedRoute>
                    <DashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route path="/produits" element={<ExplorationPage />} />
              <Route path="/produit/:id" element={<ProductDetailPage />} />
              <Route path="/exploration" element={<ExplorationPage />} />
              <Route
                path="/best-sellers"
                element={
                  <ProtectedRoute>
                    <BestSellersPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/seller/products"
                element={
                  <ProtectedRoute allowedRoles={['VENDEUR']}>
                    <InventoryPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/seller/products/new"
                element={
                  <ProtectedRoute allowedRoles={['VENDEUR']}>
                    <ProductFormPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/seller/products/edit/:id"
                element={
                  <ProtectedRoute allowedRoles={['VENDEUR']}>
                    <ProductFormPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/seller/products/prediction/:id"
                element={
                  <ProtectedRoute allowedRoles={['VENDEUR']}>
                    <PredictionPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/seller/sales/new"
                element={
                  <ProtectedRoute allowedRoles={['VENDEUR']}>
                    <CreateSalePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/users"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <UserManagementPage />
                  </ProtectedRoute>
                }
              />
            </Routes>
          </div>
        </div>
      </Router>
    </AuthProvider >
  );
}

export default App;
