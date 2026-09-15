import React, { useEffect, useRef, useState } from "react";
import {
  Routes,
  Route,
  Navigate,
  Link,
  useNavigate
} from "react-router-dom";

import {
  Home,
  Search,
  Library,
  User,
  LogOut,
  Play,
  Pause,
  Crown,
  History,
  Volume2,
  VolumeX
} from "lucide-react";

import api from "./api";

// ======================================================
// LOGIN / REGISTER
// ======================================================

function Login({ register = false, onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  async function submit(e) {
    e.preventDefault();
    setError("");

    try {
      const url = register
        ? "/api/auth/register"
        : "/api/auth/login";

      const { data } = await api.post(url, {
        email,
        password
      });

      // Save authentication information
      localStorage.setItem("token", data.token);
      localStorage.setItem("userId", data.userId);
      localStorage.setItem("plan", data.plan);

      // Reload so App reads the newly stored token
      onLogin();
      navigate("/");

    } catch (err) {
      console.error("Authentication failed:", err);

      setError(
        err.response?.data?.message ||
        err.response?.data ||
        "Request failed"
      );
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">

        <div className="brand">
          ♪ SoundFlow
        </div>

        <h1>
          {register
            ? "Create account"
            : "Welcome back"}
        </h1>

        <p>
          {register
            ? "Start listening for free."
            : "Log in to continue listening."}
        </p>

        <form onSubmit={submit}>

          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) =>
              setEmail(e.target.value)
            }
            required
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) =>
              setPassword(e.target.value)
            }
            required
          />

          {error && (
            <div className="error">
              {error}
            </div>
          )}

          <button
            type="submit"
            className="primary"
          >
            {register
              ? "Create account"
              : "Log in"}
          </button>

        </form>

        <Link
          to={
            register
              ? "/login"
              : "/register"
          }
          className="switch"
        >
          {register
            ? "Already have an account? Log in"
            : "New here? Create an account"}
        </Link>

      </div>
    </div>
  );
}

// ======================================================
// FORMAT TIME
// ======================================================

function formatTime(seconds) {
  if (!Number.isFinite(seconds)) {
    return "0:00";
  }

  const minutes = Math.floor(seconds / 60);
  const remainingSeconds =
    Math.floor(seconds % 60);

  return `${minutes}:${remainingSeconds
    .toString()
    .padStart(2, "0")}`;
}

// ======================================================
// LAYOUT + MUSIC PLAYER
// ======================================================

function Layout({
  children,
  currentSong,
  playing,
  currentTime,
  duration,
  volume,
  togglePlay,
  seek,
  changeVolume,
  logout
}) {
  return (
    <div className="app">

      {/* SIDEBAR */}

      <aside>

        <div className="logo">
          ♪ SoundFlow
        </div>

        <nav>

          <Link to="/">
            <Home size={19} />
            <span>Home</span>
          </Link>

          <Link to="/search">
            <Search size={19} />
            <span>Search</span>
          </Link>

          <Link to="/playlists">
            <Library size={19} />
            <span>Playlists</span>
          </Link>

          <Link to="/history">
            <History size={19} />
            <span>History</span>
          </Link>

          <Link to="/profile">
            <User size={19} />
            <span>Profile</span>
          </Link>

        </nav>

        <div className="plan-mini">

          <Crown size={18} />

          <span>
            {localStorage.getItem("plan") || "FREE"} plan
          </span>

        </div>

        <button
          className="logout"
          onClick={logout}
        >
          <LogOut size={18} />
          <span>Log out</span>
        </button>

      </aside>

      {/* MAIN CONTENT */}

      <main>
        {children}
      </main>

      {/* MUSIC PLAYER */}

      <footer className="player">

        <div className="now">

          {currentSong ? (
            <>
              <img
                src={currentSong.coverUrl}
                alt={currentSong.title}
              />

              <div>

                <b>
                  {currentSong.title}
                </b>

                <small>
                  {currentSong.artist}
                </small>

              </div>
            </>
          ) : (
            <span>
              Select a song
            </span>
          )}

        </div>

        <button
          className="play-btn"
          onClick={togglePlay}
          disabled={!currentSong}
          title={
            playing
              ? "Pause"
              : "Play"
          }
        >
          {playing ? (
            <Pause
              size={20}
              fill="currentColor"
            />
          ) : (
            <Play
              size={20}
              fill="currentColor"
            />
          )}
        </button>

        <div className="player-bar">

          <span>
            {formatTime(currentTime)}
          </span>

          <input
            type="range"
            min="0"
            max={duration || 0}
            step="0.1"
            value={
              Math.min(
                currentTime,
                duration || 0
              )
            }
            onChange={(e) =>
              seek(
                Number(e.target.value)
              )
            }
            disabled={!currentSong}
          />

          <span>
            {formatTime(duration)}
          </span>

        </div>

        <div className="volume-control">

          <button
            onClick={() =>
              changeVolume(
                volume > 0 ? 0 : 1
              )
            }
            title={
              volume > 0
                ? "Mute"
                : "Unmute"
            }
          >
            {volume > 0 ? (
              <Volume2 size={19} />
            ) : (
              <VolumeX size={19} />
            )}
          </button>

          <input
            type="range"
            min="0"
            max="1"
            step="0.01"
            value={volume}
            onChange={(e) =>
              changeVolume(
                Number(e.target.value)
              )
            }
          />

        </div>

      </footer>

    </div>
  );
}

// ======================================================
// HOME
// ======================================================

function HomePage({ play }) {
  const [songs, setSongs] = useState([]);

  useEffect(() => {

    api
      .get("/api/songs")
      .then((response) => {

        console.log(
          "Songs received:",
          response.data
        );

        setSongs(response.data);

      })
      .catch((error) => {

        console.error(
          "Failed to load songs:",
          error
        );

      });

  }, []);

  return (
    <section>

      <header className="top">

        <div>

          <h1>
            Good afternoon
          </h1>

          <p>
            Pick something you love.
          </p>

        </div>

      </header>

      <div className="hero">

        <div>

          <span>
            YOUR MUSIC SPACE
          </span>

          <h2>
            Find your next favorite song.
          </h2>

          <p>
            Search, play, build playlists and
            keep your listening history in one place.
          </p>

        </div>

        <div className="hero-disc">
          ♪
        </div>

      </div>

      <h2 className="section-title">
        Made for you
      </h2>

      <div className="song-grid">

        {songs.map((song) => (
          <SongCard
            key={song.id}
            song={song}
            play={play}
          />
        ))}

      </div>

    </section>
  );
}

// ======================================================
// SONG CARD
// ======================================================

function SongCard({ song, play }) {

  function handleClick() {
    console.log(
      "Song clicked:",
      song
    );

    play(song);
  }

  return (
    <div
      className="song-card"
      onClick={handleClick}
    >

      <div className="cover-wrap">

        <img
          src={song.coverUrl}
          alt={song.title}
        />

        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            play(song);
          }}
        >
          <Play
            size={20}
            fill="currentColor"
          />
        </button>

      </div>

      <b>
        {song.title}
      </b>

      <span>
        {song.artist}
      </span>

    </div>
  );
}

// ======================================================
// SEARCH
// ======================================================

function SearchPage({ play }) {

  const [q, setQ] = useState("");
  const [songs, setSongs] = useState([]);

  async function search(e) {

    e.preventDefault();

    if (!q.trim()) {
      setSongs([]);
      return;
    }

    try {

      const { data } =
        await api.get(
          "/api/search",
          {
            params: {
              q: q.trim()
            }
          }
        );

      console.log(
        "Search results:",
        data
      );

      setSongs(data);

    } catch (err) {

      console.error(
        "Search failed:",
        err
      );

      if (
        err.response?.status === 429
      ) {

        alert(
          "Too many requests, please try again later"
        );

      }

    }
  }

  return (
    <section>

      <header className="top">

        <h1>
          Search
        </h1>

      </header>

      <form
        className="search"
        onSubmit={search}
      >

        <Search />

        <input
          value={q}
          onChange={(e) =>
            setQ(e.target.value)
          }
          placeholder="What do you want to listen to?"
        />

      </form>

      <h2 className="section-title">
        Results
      </h2>

      <div className="song-grid">

        {songs.map((song) => (
          <SongCard
            key={song.id}
            song={song}
            play={play}
          />
        ))}

      </div>

    </section>
  );
}

// ======================================================
// PLAYLISTS
// ======================================================

function Playlists() {

  const [items, setItems] = useState([]);
  const [name, setName] = useState("");

  async function load() {

    try {

      const { data } =
        await api.get(
          "/api/playlists"
        );

      setItems(data);

    } catch (err) {

      console.error(
        "Failed to load playlists:",
        err
      );

    }
  }

  useEffect(() => {
    load();
  }, []);

  async function create(e) {

    e.preventDefault();

    if (!name.trim()) {
      return;
    }

    try {

      await api.post(
        "/api/playlists",
        {
          name: name.trim()
        }
      );

      setName("");

      await load();

    } catch (err) {

      console.error(
        "Failed to create playlist:",
        err
      );

      if (
        err.response?.status === 429
      ) {

        alert(
          "Too many requests, please try again later"
        );

      }

    }
  }

  return (
    <section>

      <header className="top">

        <h1>
          Your Playlists
        </h1>

      </header>

      <form
        className="create"
        onSubmit={create}
      >

        <input
          value={name}
          onChange={(e) =>
            setName(e.target.value)
          }
          placeholder="Playlist name"
        />

        <button
          className="primary"
          type="submit"
        >
          Create
        </button>

      </form>

      <div className="playlist-list">

        {items.map((playlist) => (

          <div
            className="playlist"
            key={playlist.id}
          >

            <div className="playlist-icon">
              ♫
            </div>

            <div>

              <b>
                {playlist.name}
              </b>

              <span>
                {playlist.songs?.length || 0} songs
              </span>

            </div>

          </div>

        ))}

      </div>

    </section>
  );
}

// ======================================================
// PROFILE
// ======================================================

function Profile() {

  const [profile, setProfile] =
    useState(null);

  useEffect(() => {

    api
      .get("/api/profile")
      .then((response) => {

        console.log(
          "Profile:",
          response.data
        );

        setProfile(response.data);

      })
      .catch((error) => {

        console.error(
          "Failed to load profile:",
          error
        );

      });

  }, []);

  if (!profile) {

    return (
      <section>

        <header className="top">
          <h1>Profile</h1>
        </header>

        <div className="empty">
          Loading profile...
        </div>

      </section>
    );
  }

  return (
    <section>

      <header className="top">

        <h1>
          Profile
        </h1>

      </header>

      <div className="profile-card">

        <div className="avatar">
          {profile.email
            ?.charAt(0)
            .toUpperCase()}
        </div>

        <h2>
          {profile.email}
        </h2>

        <span>
          User ID: {profile.userId}
        </span>

        <div
          className={
            "badge " +
            profile.plan
          }
        >
          {profile.plan} PLAN
        </div>

      </div>

      <div className="upgrade">

        <Crown />

        <div>

          <b>
            Upgrade your experience
          </b>

          <p>
            Move from FREE to PRO or PREMIUM
            for higher API limits.
          </p>

        </div>

        <button className="primary">
          View plans
        </button>

      </div>

    </section>
  );
}

// ======================================================
// HISTORY
// ======================================================

function HistoryPage() {

  const [items, setItems] =
    useState([]);

  useEffect(() => {

    api
      .get("/api/history")
      .then((response) => {

        console.log(
          "History:",
          response.data
        );

        setItems(response.data);

      })
      .catch((error) => {

        console.error(
          "Failed to load history:",
          error
        );

      });

  }, []);

  return (
    <section>

      <header className="top">

        <h1>
          Listening History
        </h1>

      </header>

      {items.length === 0 ? (

        <div className="empty">
          No listening history yet.
          Play a song to see it here.
        </div>

      ) : (

        <div className="history">

          {items.map((item) => (

            <div key={item.id}>

              <History />

              <span>
                Song ID {item.songId}
              </span>

              <small>
                {new Date(
                  item.playedAt
                ).toLocaleString()}
              </small>

            </div>

          ))}

        </div>

      )}

    </section>
  );
}

// ======================================================
// PROTECTED APPLICATION
// ======================================================

function Protected({ onLogout }) {
  const navigate = useNavigate();
  const [currentSong, setCurrentSong] =
    useState(null);

  const [playing, setPlaying] =
    useState(false);

  const [currentTime, setCurrentTime] =
    useState(0);

  const [duration, setDuration] =
    useState(0);

  const [volume, setVolume] =
    useState(1);

  const audioRef =
    useRef(null);

  // ====================================================
  // PLAY SONG
  // ====================================================

  function play(song) {

    console.log(
      "Selected song:",
      song
    );

    console.log(
      "Audio URL:",
      song.audioUrl
    );

    if (!song.audioUrl) {

      console.error(
        "No audioUrl found:",
        song
      );

      alert(
        "This song does not have an audio file."
      );

      return;
    }

    const audio =
      audioRef.current;

    if (!audio) {

      console.error(
        "Audio element not found"
      );

      return;
    }

    audio.pause();

    setPlaying(false);
    setCurrentTime(0);
    setDuration(0);

    setCurrentSong(song);

    audio.src = song.audioUrl;

    audio.volume = volume;

    audio.load();

    console.log(
      "Starting audio..."
    );

    audio
      .play()
      .then(() => {

        console.log(
          "Audio started"
        );

        console.log(
          "Audio duration:",
          audio.duration
        );

        setDuration(
          audio.duration
        );

        setPlaying(true);

      })
      .catch((error) => {

        console.error(
          "Audio playback failed:",
          error
        );

        setPlaying(false);

        alert(
          "Unable to play this song. Check your browser volume."
        );

      });

    // Backend play API

    api
      .post(
        "/api/play",
        {
          songId: song.id
        }
      )
      .then(() => {

        console.log(
          "Play API request successful."
        );

      })
      .catch((error) => {

        console.error(
          "Play API request failed:",
          error
        );

        if (
          error.response?.status === 429
        ) {

          alert(
            "Too many requests, please try again later"
          );

        }

      });
  }

  // ====================================================
  // PLAY / PAUSE
  // ====================================================

  function togglePlay() {

    const audio =
      audioRef.current;

    if (!audio || !currentSong) {
      return;
    }

    if (audio.paused) {

      console.log(
        "Starting audio..."
      );

      audio
        .play()
        .then(() => {

          console.log(
            "Audio started"
          );

          setPlaying(true);

        })
        .catch((error) => {

          console.error(
            "Playback failed:",
            error
          );

        });

    } else {

      console.log(
        "Pausing audio..."
      );

      audio.pause();

      console.log(
        "Audio paused"
      );

      setPlaying(false);
    }
  }

  // ====================================================
  // SEEK
  // ====================================================

  function seek(time) {

    const audio =
      audioRef.current;

    if (!audio) {
      return;
    }

    audio.currentTime = time;

    setCurrentTime(time);
  }

  // ====================================================
  // VOLUME
  // ====================================================

  function changeVolume(value) {

    const audio =
      audioRef.current;

    setVolume(value);

    if (audio) {
      audio.volume = value;
    }
  }

  // ====================================================
  // AUDIO EVENTS
  // ====================================================

  useEffect(() => {

    const audio =
      audioRef.current;

    if (!audio) {
      return;
    }

    function handleLoadedMetadata() {

      console.log(
        "Audio duration:",
        audio.duration
      );

      setDuration(
        audio.duration
      );
    }

    function handleTimeUpdate() {

      setCurrentTime(
        audio.currentTime
      );
    }

    function handlePlay() {

      console.log(
        "HTML AUDIO: playing"
      );

      setPlaying(true);
    }

    function handlePause() {

      console.log(
        "HTML AUDIO: paused"
      );

      setPlaying(false);
    }

    function handleEnded() {

      console.log(
        "HTML AUDIO: ended"
      );

      setPlaying(false);
      setCurrentTime(0);
    }

    function handleError() {

      console.error(
        "HTML AUDIO ERROR:",
        audio.error
      );

      setPlaying(false);
    }

    audio.addEventListener(
      "loadedmetadata",
      handleLoadedMetadata
    );

    audio.addEventListener(
      "timeupdate",
      handleTimeUpdate
    );

    audio.addEventListener(
      "play",
      handlePlay
    );

    audio.addEventListener(
      "pause",
      handlePause
    );

    audio.addEventListener(
      "ended",
      handleEnded
    );

    audio.addEventListener(
      "error",
      handleError
    );

    return () => {

      audio.removeEventListener(
        "loadedmetadata",
        handleLoadedMetadata
      );

      audio.removeEventListener(
        "timeupdate",
        handleTimeUpdate
      );

      audio.removeEventListener(
        "play",
        handlePlay
      );

      audio.removeEventListener(
        "pause",
        handlePause
      );

      audio.removeEventListener(
        "ended",
        handleEnded
      );

      audio.removeEventListener(
        "error",
        handleError
      );

    };

  }, []);

  // ====================================================
  // LOGOUT
  // ====================================================
function logout() {
  const audio = audioRef.current;

  if (audio) {
    audio.pause();
    audio.currentTime = 0;
    audio.removeAttribute("src");
    audio.load();
  }

  localStorage.removeItem("token");
  localStorage.removeItem("userId");
  localStorage.removeItem("plan");

  onLogout();
  navigate("/login");
}

  // ====================================================
  // UI
  // ====================================================

  return (
    <>
      <audio
        ref={audioRef}
        preload="auto"
      />

      <Layout
        currentSong={currentSong}
        playing={playing}
        currentTime={currentTime}
        duration={duration}
        volume={volume}
        togglePlay={togglePlay}
        seek={seek}
        changeVolume={changeVolume}
        logout={logout}
      >

        <Routes>

          <Route
            path="/"
            element={
              <HomePage
                play={play}
              />
            }
          />

          <Route
            path="/search"
            element={
              <SearchPage
                play={play}
              />
            }
          />

          <Route
            path="/playlists"
            element={
              <Playlists />
            }
          />

          <Route
            path="/history"
            element={
              <HistoryPage />
            }
          />

          <Route
            path="/profile"
            element={
              <Profile />
            }
          />

          <Route
            path="*"
            element={
              <Navigate to="/" />
            }
          />

        </Routes>

      </Layout>
    </>
  );
}

// ======================================================
// MAIN APP
// ======================================================

export default function App() {
  const [logged, setLogged] = useState(
    !!localStorage.getItem("token")
  );

  function handleLogin() {
    setLogged(true);
  }

  function handleLogout() {
    setLogged(false);
  }

  return (
    <Routes>

      <Route
        path="/login"
        element={
          logged ? (
            <Navigate to="/" />
          ) : (
            <Login onLogin={handleLogin} />
          )
        }
      />

      <Route
        path="/register"
        element={
          logged ? (
            <Navigate to="/" />
          ) : (
            <Login
              register
              onLogin={handleLogin}
            />
          )
        }
      />

      <Route
        path="/*"
        element={
          logged ? (
            <Protected onLogout={handleLogout} />
          ) : (
            <Navigate to="/login" />
          )
        }
      />

    </Routes>
  );
}