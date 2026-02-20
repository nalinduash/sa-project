import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { reservationApi, genreApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

interface Reservation {
  id: number;
  stallCode: string;
  stallSize: string;
  qrCode: string;
  reservationDate: string;
  businessName: string;
}

const Home: React.FC = () => {
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [genres, setGenres] = useState<string[]>([]);
  const [newGenre, setNewGenre] = useState('');
  const [loading, setLoading] = useState(true);
  const [showQR, setShowQR] = useState<string | null>(null);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [reservationsRes, genresRes] = await Promise.all([
        reservationApi.getMyReservations(),
        genreApi.getMyGenres(),
      ]);
      setReservations(reservationsRes.data);
      setGenres(genresRes.data);
    } catch (err) {
      console.error('Failed to fetch data', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddGenre = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newGenre.trim()) return;

    try {
      await genreApi.add(newGenre.trim());
      setGenres([...genres, newGenre.trim()]);
      setNewGenre('');
    } catch (err) {
      console.error('Failed to add genre', err);
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
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-indigo-600">Colombo International Book Fair</h1>
            <p className="text-red-600">Welcome, {user?.businessName}</p>
          </div>
          <div className="flex gap-4">
            <button
              onClick={() => navigate('/reserve')}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
            >
              Reserve Stalls
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
        {/* Reservations Section */}
        <section className="bg-white rounded-xl shadow-sm p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">My Reservations</h2>
          {reservations.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-500 mb-4">You haven't reserved any stalls yet.</p>
              <button
                onClick={() => navigate('/reserve')}
                className="px-6 py-3 bg-indigo-600 text-white rounded-lg font-semibold hover:bg-indigo-700 transition"
              >
                Reserve a Stall
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {reservations.map((reservation) => (
                <div
                  key={reservation.id}
                  className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition"
                >
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <h3 className="font-semibold text-gray-800">{reservation.stallCode}</h3>
                      <span className="inline-block px-2 py-1 text-xs font-medium bg-indigo-100 text-indigo-700 rounded">
                        {reservation.stallSize}
                      </span>
                    </div>
                    <button
                      onClick={() => setShowQR(reservation.qrCode)}
                      className="text-indigo-600 hover:text-indigo-800 text-sm font-medium"
                    >
                      View QR
                    </button>
                  </div>
                  <p className="text-sm text-gray-500">
                    Reserved: {formatDate(reservation.reservationDate)}
                  </p>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Genres Section */}
        <section className="bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Literary Genres</h2>
          <p className="text-gray-500 mb-4">
            Add the literary genres you will be displaying/selling at the exhibition.
          </p>

          <form onSubmit={handleAddGenre} className="flex gap-3 mb-4">
            <input
              type="text"
              value={newGenre}
              onChange={(e) => setNewGenre(e.target.value)}
              placeholder="e.g., Fiction, Non-fiction, Children's Books..."
              className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
            />
            <button
              type="submit"
              className="px-6 py-3 bg-indigo-600 text-white rounded-lg font-semibold hover:bg-indigo-700 transition"
            >
              Add Genre
            </button>
          </form>

          {genres.length > 0 ? (
            <div className="flex flex-wrap gap-2">
              {genres.map((genre, index) => (
                <span
                  key={index}
                  className="px-4 py-2 bg-gray-100 text-gray-700 rounded-full text-sm font-medium"
                >
                  {genre}
                </span>
              ))}
            </div>
          ) : (
            <p className="text-gray-400 italic">No genres added yet.</p>
          )}
        </section>
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
            <h3 className="text-xl font-bold text-gray-800 mb-4 text-center">Your QR Pass</h3>
            <div className="flex justify-center mb-4">
              <img
                src={`data:image/png;base64,${showQR}`}
                alt="QR Code"
                className="w-64 h-64"
              />
            </div>
            <p className="text-sm text-gray-500 text-center mb-4">
              Present this QR code at the exhibition entrance.
            </p>
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

export default Home;
