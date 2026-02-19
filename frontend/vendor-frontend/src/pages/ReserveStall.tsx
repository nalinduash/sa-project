import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { stallApi, reservationApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

interface Stall {
  id: number;
  stallCode: string;
  size: string;
  location: string;
  price: number;
  isAvailable: boolean;
  rowPosition: number;
  columnPosition: number;
}

const ReserveStall: React.FC = () => {
  const [stalls, setStalls] = useState<Stall[]>([]);
  const [selectedStalls, setSelectedStalls] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [showConfirm, setShowConfirm] = useState(false);
  const [reserving, setReserving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchStalls();
  }, []);

  const fetchStalls = async () => {
    try {
      const response = await stallApi.getAll();
      setStalls(response.data);
    } catch (err) {
      console.error('Failed to fetch stalls', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStallClick = (stall: Stall) => {
    if (!stall.isAvailable) return;

    if (selectedStalls.includes(stall.id)) {
      setSelectedStalls(selectedStalls.filter((id) => id !== stall.id));
    } else {
      if (selectedStalls.length >= 3) {
        setError('You can reserve a maximum of 3 stalls');
        setTimeout(() => setError(''), 3000);
        return;
      }
      setSelectedStalls([...selectedStalls, stall.id]);
    }
  };

  const handleConfirmReservation = async () => {
    setReserving(true);
    setError('');

    try {
      await reservationApi.create(selectedStalls);
      setSuccess('Reservation successful! Check your email for confirmation with QR code.');
      setSelectedStalls([]);
      setShowConfirm(false);
      fetchStalls();
      setTimeout(() => {
        navigate('/home');
      }, 2000);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      setError(error.response?.data?.message || 'Reservation failed. Please try again.');
    } finally {
      setReserving(false);
    }
  };

  const getSizeColor = (size: string, isAvailable: boolean) => {
    if (!isAvailable) return 'bg-gray-400 border-gray-500';

    switch (size) {
      case 'SMALL':
        return 'bg-green-100 border-green-500 hover:bg-green-200';
      case 'MEDIUM':
        return 'bg-blue-100 border-blue-500 hover:bg-blue-200';
      case 'LARGE':
        return 'bg-purple-100 border-purple-500 hover:bg-purple-200';
      default:
        return 'bg-gray-100 border-gray-500';
    }
  };

  const getSelectedColor = (size: string) => {
    switch (size) {
      case 'SMALL':
        return 'bg-green-500 border-green-600 text-white';
      case 'MEDIUM':
        return 'bg-blue-500 border-blue-600 text-white';
      case 'LARGE':
        return 'bg-purple-500 border-purple-600 text-white';
      default:
        return 'bg-indigo-500 border-indigo-600 text-white';
    }
  };

  const getSizePrice = (size: string) => {
    switch (size) {
      case 'SMALL':
        return 'Rs. 25,000';
      case 'MEDIUM':
        return 'Rs. 45,000';
      case 'LARGE':
        return 'Rs. 75,000';
      default:
        return '';
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Group stalls by rows
  const stallsByRow = stalls.reduce((acc, stall) => {
    if (!acc[stall.rowPosition]) {
      acc[stall.rowPosition] = [];
    }
    acc[stall.rowPosition].push(stall);
    return acc;
  }, {} as Record<number, Stall[]>);

  const selectedStallDetails = stalls.filter((s) => selectedStalls.includes(s.id));
  const totalPrice = selectedStallDetails.reduce((sum, s) => sum + s.price, 0);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-xl text-gray-600">Loading stalls...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-indigo-600">Colombo International Book Fair</h1>
            <p className="text-gray-600">Welcome, {user?.businessName}</p>
          </div>
          <div className="flex gap-4">
            <button
              onClick={() => navigate('/home')}
              className="px-4 py-2 text-gray-600 hover:text-indigo-600 transition"
            >
              My Dashboard
            </button>
            <button
              onClick={handleLogout}
              className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        {error && (
          <div className="bg-red-50 text-red-600 p-4 rounded-lg mb-6">{error}</div>
        )}
        {success && (
          <div className="bg-green-50 text-green-600 p-4 rounded-lg mb-6">{success}</div>
        )}

        {/* Legend */}
        <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">Stall Legend</h2>
          <div className="flex flex-wrap gap-6">
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 bg-green-100 border-2 border-green-500 rounded"></div>
              <span className="text-sm text-gray-600">Small - Rs. 25,000</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 bg-blue-100 border-2 border-blue-500 rounded"></div>
              <span className="text-sm text-gray-600">Medium - Rs. 45,000</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 bg-purple-100 border-2 border-purple-500 rounded"></div>
              <span className="text-sm text-gray-600">Large - Rs. 75,000</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 bg-gray-400 border-2 border-gray-500 rounded"></div>
              <span className="text-sm text-gray-600">Reserved</span>
            </div>
          </div>
          <p className="mt-4 text-sm text-gray-500">
            Click on available stalls to select. You can reserve up to 3 stalls per business.
          </p>
        </div>

        {/* Stall Map */}
        <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-6">Exhibition Venue Map</h2>
          <div className="overflow-x-auto">
            <div className="min-w-fit space-y-4">
              {Object.entries(stallsByRow).map(([rowIdx, rowStalls]) => (
                <div key={rowIdx} className="flex items-center gap-4">
                  <div className="w-16 text-center font-semibold text-gray-700">
                    Zone {String.fromCharCode(65 + parseInt(rowIdx))}
                  </div>
                  <div className="flex gap-3">
                    {rowStalls
                      .sort((a, b) => a.columnPosition - b.columnPosition)
                      .map((stall) => {
                        const isSelected = selectedStalls.includes(stall.id);
                        return (
                          <button
                            key={stall.id}
                            onClick={() => handleStallClick(stall)}
                            disabled={!stall.isAvailable}
                            className={`w-20 h-20 rounded-lg border-2 flex flex-col items-center justify-center transition-all ${
                              isSelected
                                ? getSelectedColor(stall.size)
                                : getSizeColor(stall.size, stall.isAvailable)
                            } ${!stall.isAvailable ? 'cursor-not-allowed' : 'cursor-pointer'}`}
                          >
                            <span className="font-bold text-sm">{stall.stallCode}</span>
                            <span className="text-xs mt-1">{stall.size.charAt(0)}</span>
                          </button>
                        );
                      })}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Selected Stalls Summary */}
        {selectedStalls.length > 0 && (
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-800 mb-4">Selected Stalls</h2>
            <div className="space-y-2 mb-4">
              {selectedStallDetails.map((stall) => (
                <div key={stall.id} className="flex justify-between text-gray-600">
                  <span>{stall.stallCode} - {stall.size}</span>
                  <span>{getSizePrice(stall.size)}</span>
                </div>
              ))}
              <div className="border-t pt-2 mt-2 flex justify-between font-semibold text-gray-800">
                <span>Total</span>
                <span>Rs. {totalPrice.toLocaleString()}</span>
              </div>
            </div>
            <button
              onClick={() => setShowConfirm(true)}
              className="w-full bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700 transition"
            >
              Confirm Reservation
            </button>
          </div>
        )}
      </main>

      {/* Confirmation Modal */}
      {showConfirm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
            <h3 className="text-xl font-bold text-gray-800 mb-4">Confirm Reservation</h3>
            <p className="text-gray-600 mb-4">
              Are you sure you want to reserve {selectedStalls.length} stall(s)? A confirmation
              email with your QR pass will be sent to your email address.
            </p>
            <div className="bg-gray-50 rounded-lg p-4 mb-4">
              <p className="font-semibold text-gray-800">Selected Stalls:</p>
              <ul className="mt-2 space-y-1">
                {selectedStallDetails.map((stall) => (
                  <li key={stall.id} className="text-gray-600">
                    {stall.stallCode} - {stall.size}
                  </li>
                ))}
              </ul>
              <p className="mt-2 font-semibold text-gray-800">
                Total: Rs. {totalPrice.toLocaleString()}
              </p>
            </div>
            <div className="flex gap-3">
              <button
                onClick={() => setShowConfirm(false)}
                className="flex-1 py-3 border border-gray-300 rounded-lg font-semibold text-gray-700 hover:bg-gray-50 transition"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmReservation}
                disabled={reserving}
                className="flex-1 bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700 transition disabled:opacity-50"
              >
                {reserving ? 'Reserving...' : 'Confirm'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReserveStall;
