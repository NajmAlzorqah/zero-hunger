"use client";

import { useEffect, useState } from "react";
import dynamic from "next/dynamic";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import { donationAPI, claimAPI } from "@/lib/api";
import CreateDonationForm from "@/components/CreateDonationForm";
import ClaimActions from "@/components/ClaimActions";

// Dynamic import for Leaflet map (no SSR)
const DonationMap = dynamic(() => import("@/components/DonationMap"), {
  ssr: false,
  loading: () => (
    <div className="h-[400px] bg-gray-200 rounded-lg flex items-center justify-center">
      Loading map...
    </div>
  ),
});

export default function DashboardPage() {
  const { user, logout, loading } = useAuth();
  const router = useRouter();
  const [donations, setDonations] = useState([]);
  const [claims, setClaims] = useState([]);
  const [isLoadingData, setIsLoadingData] = useState(true);
  const [viewMode, setViewMode] = useState("list"); // 'list' or 'map'

  useEffect(() => {
    if (!loading && !user) {
      router.push("/login");
      return;
    }

    if (user) {
      fetchData();
    }
  }, [user, loading, router]);

  const fetchData = async () => {
    setIsLoadingData(true);
    try {
      // Check if user.roles is a Set (from Jakarta EE) or Array
      const roles = Array.isArray(user.roles)
        ? user.roles
        : Array.from(user.roles || []);

      if (roles.includes("donor")) {
        const response = await donationAPI.myDonations();
        // Jakarta EE returns array directly, not wrapped in {data: []}
        setDonations(Array.isArray(response.data) ? response.data : []);
      } else if (roles.includes("volunteer")) {
        const [donationsRes, claimsRes] = await Promise.all([
          donationAPI.list(),
          claimAPI.list(),
        ]);
        // Jakarta EE returns arrays directly
        setDonations(Array.isArray(donationsRes.data) ? donationsRes.data : []);
        setClaims(Array.isArray(claimsRes.data) ? claimsRes.data : []);
      }
    } catch (error) {
      console.error("Failed to fetch data:", error);
    } finally {
      setIsLoadingData(false);
    }
  };

  const handleClaim = async (donationId) => {
    try {
      const response = await donationAPI.claim(donationId);
      alert(`Donation claimed! Pickup code: ${response.data.pickup_code}`);
      fetchData();
    } catch (error) {
      alert(error.response?.data?.message || "Failed to claim");
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        Loading...
      </div>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Navigation */}
      <nav className="bg-white shadow p-4">
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-xl font-bold text-green-600">🌱 ZeroHunger</h1>
          <div className="flex items-center gap-4">
            <span className="font-medium">{user.name}</span>
            <span className="text-sm px-2 py-1 bg-green-100 text-green-800 rounded">
              {Array.isArray(user.roles)
                ? user.roles[0]
                : user.roles
                ? Array.from(user.roles)[0]
                : "user"}
            </span>
            <span className="text-sm text-gray-600">
              ⭐ Impact: {user.impactScore || 0}
            </span>
            <button
              onClick={logout}
              className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
            >
              Logout
            </button>
          </div>
        </div>
      </nav>

      <div className="container mx-auto p-8">
        {isLoadingData ? (
          <div className="text-center py-8">Loading data...</div>
        ) : (
          <>
            {/* Donor Dashboard */}
            {(Array.isArray(user.roles)
              ? user.roles
              : Array.from(user.roles || [])
            ).includes("donor") && (
              <div>
                <CreateDonationForm onSuccess={fetchData} />

                <h2 className="text-2xl font-bold mb-4">📦 My Donations</h2>
                {donations.length === 0 ? (
                  <p className="text-gray-500">
                    No donations yet. Create one above!
                  </p>
                ) : (
                  <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {donations.map((donation) => (
                      <div
                        key={donation.id}
                        className="bg-white p-4 rounded shadow"
                      >
                        <h3 className="font-bold text-lg">{donation.title}</h3>
                        <p className="text-sm text-gray-600 mt-1">
                          {donation.description}
                        </p>
                        <div className="mt-3 flex justify-between items-center">
                          <span className="text-sm font-medium">
                            {donation.quantity_kg} kg
                          </span>
                          <span
                            className={`px-3 py-1 rounded text-sm ${
                              donation.status === "available"
                                ? "bg-green-100 text-green-800"
                                : donation.status === "reserved"
                                ? "bg-yellow-100 text-yellow-800"
                                : "bg-blue-100 text-blue-800"
                            }`}
                          >
                            {donation.status}
                          </span>
                        </div>
                        {donation.pickupCode && (
                          <p className="mt-2 text-sm font-mono bg-gray-100 p-2 rounded">
                            🔑 Pickup Code:{" "}
                            <strong>{donation.pickupCode}</strong>
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* Volunteer Dashboard */}
            {(Array.isArray(user.roles)
              ? user.roles
              : Array.from(user.roles || [])
            ).includes("volunteer") && (
              <div>
                {/* View Toggle */}
                <div className="mb-4 flex gap-2">
                  <button
                    onClick={() => setViewMode("list")}
                    className={`px-4 py-2 rounded ${
                      viewMode === "list"
                        ? "bg-green-600 text-white"
                        : "bg-gray-200"
                    }`}
                  >
                    📋 List View
                  </button>
                  <button
                    onClick={() => setViewMode("map")}
                    className={`px-4 py-2 rounded ${
                      viewMode === "map"
                        ? "bg-green-600 text-white"
                        : "bg-gray-200"
                    }`}
                  >
                    🗺️ Map View
                  </button>
                </div>

                <h2 className="text-2xl font-bold mb-4">
                  🍽️ Available Donations
                </h2>

                {viewMode === "map" ? (
                  <div className="mb-8">
                    <DonationMap donations={donations} onClaim={handleClaim} />
                  </div>
                ) : donations.length === 0 ? (
                  <p className="text-gray-500 mb-8">No donations available.</p>
                ) : (
                  <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 mb-8">
                    {donations.map((donation) => (
                      <div
                        key={donation.id}
                        className="bg-white p-4 rounded shadow"
                      >
                        <h3 className="font-bold text-lg">{donation.title}</h3>
                        <p className="text-sm text-gray-600 mt-1">
                          {donation.description}
                        </p>
                        <p className="text-sm mt-2">
                          📍 Donor: {donation.donor?.name || "Anonymous"}
                        </p>
                        <div className="mt-3 flex justify-between items-center">
                          <span className="text-sm font-medium">
                            {donation.quantityKg} kg
                          </span>
                          <button
                            onClick={() => handleClaim(donation.id)}
                            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
                          >
                            Claim
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                <h2 className="text-2xl font-bold mb-4 mt-8">🚴 My Claims</h2>
                {claims.length === 0 ? (
                  <p className="text-gray-500">No active claims.</p>
                ) : (
                  <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {claims.map((claim) => (
                      <div
                        key={claim.id}
                        className="bg-white p-4 rounded shadow"
                      >
                        <h3 className="font-bold text-lg">
                          {claim.donation?.title}
                        </h3>
                        <p className="text-sm text-gray-600">
                          Status:{" "}
                          <span
                            className={`font-semibold ${
                              claim.status === "delivered"
                                ? "text-green-600"
                                : claim.status === "picked_up"
                                ? "text-blue-600"
                                : "text-yellow-600"
                            }`}
                          >
                            {claim.status}
                          </span>
                        </p>
                        <p className="text-sm mt-1">
                          Quantity: {claim.donation?.quantity_kg} kg
                        </p>
                        <ClaimActions claim={claim} onUpdate={fetchData} />
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* Admin Dashboard */}
            {(Array.isArray(user.roles)
              ? user.roles
              : Array.from(user.roles || [])
            ).includes("admin") && (
              <div>
                <h2 className="text-2xl font-bold mb-4">👑 Admin Dashboard</h2>
                <p className="text-gray-600">
                  Welcome, Administrator. Full admin features coming in Stage
                  11-12.
                </p>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
