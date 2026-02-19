import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { employeeApi } from '../services/api';
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

interface Reservation {
  id: number;
  stallCode: string;
  stallSize: string;
  qrCode: string;
  reservationDate: string;
  businessName: string;
}

const Dashboard: React.FC = () => {
  const [stalls, setStalls] = useState<Stall[]>([]);
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'stalls' | 'reservations'>('stalls');
  const [showQR, setShowQR] = useState<string | null>(null);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [stallsRes, reservationsRes] = await Promise.all([
        employeeApi.getAllStalls(),
        employeeApi.getAllReservations(),
      ]);
      setStalls(stallsRes.data);
      setReservations(reservationsRes.data);
    } catch (err) {
      console.error('Failed to fetch data', err);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getSizeColor = (size: string, isAvailable: boolean) => {
    if (!isAvailable) return 'bg-gray-400 border-gray-500';

    switch (size) {
      case 'SMALL':
        return 'bg-green-100 border-green-500';
      case 'MEDIUM':
        return 'bg-blue-100 border-blue-500';
      case 'LARGE':
        return 'bg-purple-100 border-purple-500';
      default:
        return 'bg-gray-100 border-gray-500';
    }
  };

  // Group stalls by rows
  const stallsByRow = stalls.reduce((acc, stall) => {
    if (!acc[stall.rowPosition]) {
      acc[stall.rowPosition] = [];
    }
    acc[stall.rowPosition].push(stall);
    return acc;
  }, {} as Record<number, Stall[]>);

  const stats = {
    totalStalls: stalls.length,
    availableStalls: stalls.filter((s) => s.isAvailable).length,
    reservedStalls: stalls.filter((s) => !s.isAvailable).length,
    totalReservations: reservations.length,
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-xl text-gray-600">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-slate-800 text-white shadow-lg">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold">Employee Portal</h1>
            <p className="text-slate-300">Colombo International Book Fair</p>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-slate-300">Welcome, {user?.businessName}</span>
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
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <div className="bg-white rounded-xl shadow-sm p-6">
            <p className="text-sm text-gray-500">Total Stalls</p>
            <p className="text-3xl font-bold text-gray-800">{stats.totalStalls}</p>
          </div>
          <div className="bg-white rounded-xl shadow-sm p-6">
            <p className="text-sm text-gray-500">Available</p>
            <p className="text-3xl font-bold text-green-600">{stats.availableStalls}</p>
          </div>
          <div className="bg-white rounded-xl shadow-sm p-6">
            <p className="text-sm text-gray-500">Reserved</p>
            <p className="text-3xl font-bold text-red-600">{stats.reservedStalls}</p>
          </div>
          <div className="bg-white rounded-xl shadow-sm p-6">
            <p className="text-sm text-gray-500">Total Reservations</p>
            <p className="text-3xl font-bold text-indigo-600">{stats.totalReservations}</p>
          </div>
        </div>

        {/* Tabs */}
        <div className="flex gap-4 mb-6">
          <button
            onClick={() => setActiveTab('stalls')}
            className={`px-6 py-3 rounded-lg font-semibold transition ${
              activeTab === 'stalls'
                ? 'bg-slate-800 text-white'
                : 'bg-white text-gray-600 hover:bg-gray-100'
            }`}
          >
            Stall Map
          </button>
          <button
            onClick={() => setActiveTab('reservations')}
            className={`px-6 py-3 rounded-lg font-semibold transition ${
              activeTab === 'reservations'
                ? 'bg-slate-800 text-white'
                : 'bg-white text-gray-600 hover:bg-gray-100'
            }`}
          >
            Reservations
          </button>
        </div>

        {/* Content */}
        {activeTab === 'stalls' ? (
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-800 mb-4">Exhibition Venue Map</h2>
            
            {/* Legend */}
            <div className="flex flex-wrap gap-6 mb-6">
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 bg-green-100 border-2 border-green-500 rounded"></div>
                <span className="text-sm text-gray-600">Small - Available</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 bg-blue-100 border-2 border-blue-500 rounded"></div>
                <span className="text-sm text-gray-600">Medium - Available</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 bg-purple-100 border-2 border-purple-500 rounded"></div>
                <span className="text-sm text-gray-600">Large - Available</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 bg-gray-400 border-2 border-gray-500 rounded"></div>
                <span className="text-sm text-gray-600">Reserved</span>
              </div>
            </div>

            {/* Stall Grid */}
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
                        .map((stall) => (
                          <div
                            key={stall.id}
                            className={`w-20 h-20 rounded-lg border-2 flex flex-col items-center justify-center ${getSizeColor(
                              stall.size,
                              stall.isAvailable
                            )}`}
                          >
                            <span className="font-bold text-sm text-gray-800">{stall.stallCode}</span>
                            <span className="text-xs mt-1 text-gray-600">{stall.size.charAt(0)}</span>
                            {!stall.isAvailable && (
                              <span className="text-xs text-gray-600">Reserved</span>
                            )}
                          </div>
                        ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ) : (
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-800 mb-4">All Reservations</h2>
            {reservations.length === 0 ? (
              <p className="text-gray-500 text-center py-8">No reservations yet.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-gray-200">
                      <th className="text-left py-3 px-4 font-semibold text-gray-700">Stall Code</th>
                      <th className="text-left py-3 px-4 font-semibold text-gray-700">Size</th>
                      <th className="text-left py-3 px-4 font-semibold text-gray-700">Business</th>
                      <th className="text-left py-3 px-4 font-semibold text-gray-700">Date</th>
                      <th className="text-left py-3 px-4 font-semibold text-gray-700">QR Code</th>
                    </tr>
                  </thead>
                  <tbody>
                    {reservations.map((reservation) => (
                      <tr key={reservation.id} className="border-b border-gray-100 hover:bg-gray-50">
                        <td className="py-3 px-4 font-medium text-gray-800">{reservation.stallCode}</td>
                        <td className="py-3 px-4">
                          <span className="inline-block px-2 py-1 text-xs font-medium bg-indigo-100 text-indigo-700 rounded">
                            {reservation.stallSize}
                          </span>
                        </td>
                        <td className="py-3 px-4 text-gray-600">{reservation.businessName}</td>
                        <td className="py-3 px-4 text-gray-600 text-sm">
                          {formatDate(reservation.reservationDate)}
                        </td>
                        <td className="py-3 px-4">
                          <button
                            onClick={() => setShowQR(reservation.qrCode)}
                            className="text-indigo-600 hover:text-indigo-800 text-sm font-medium"
                          >
                            View QR
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </main>

      {/* QR Code Modal */}
      {showQR && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
          onClick={() => setShowQR(null)}
        >
          <div
            className="bg-white rounded-2xl shadow-xl max-w-sm w-full p-6"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="text-xl font-bold text-gray-800 mb-4 text-center">QR Pass</h3>
            <div className="flex justify-center mb-4">
              <img
                src={`data:image/png;base64,${showQR}`}
                alt="QR Code"
                className="w-64 h-64"
              />
            </div>
            <button
              onClick={() => setShowQR(null)}
              className="w-full py-3 border border-gray-300 rounded-lg font-semibold text-gray-700 hover:bg-gray-50 transition"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
