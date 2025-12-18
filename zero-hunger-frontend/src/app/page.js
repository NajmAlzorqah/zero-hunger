"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import Link from "next/link";

export default function Home() {
  const router = useRouter();
  const { user, loading } = useAuth();

  useEffect(() => {
    if (!loading && user) {
      router.push("/dashboard");
    }
  }, [user, loading, router]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-xl">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-green-50 to-white">
      {/* Hero Section */}
      <div className="container mx-auto px-4 py-16">
        <div className="text-center">
          <h1 className="text-5xl font-bold text-green-600 mb-4">
            🌱 ZeroHunger
          </h1>
          <p className="text-xl text-gray-700 mb-8">
            Fighting food waste, feeding communities
          </p>
          <p className="text-lg text-gray-600 mb-12 max-w-2xl mx-auto">
            Connect food donors with volunteers to deliver surplus food to those
            in need. Together, we can reduce waste and fight hunger.
          </p>

          <div className="flex gap-4 justify-center">
            <Link
              href="/register"
              className="bg-green-600 text-white px-8 py-3 rounded-lg text-lg font-semibold hover:bg-green-700 transition"
            >
              Get Started
            </Link>
            <Link
              href="/login"
              className="bg-white border-2 border-green-600 text-green-600 px-8 py-3 rounded-lg text-lg font-semibold hover:bg-green-50 transition"
            >
              Login
            </Link>
          </div>
        </div>

        {/* Features */}
        <div className="grid md:grid-cols-3 gap-8 mt-16">
          <div className="bg-white p-6 rounded-lg shadow-md text-center">
            <div className="text-4xl mb-4">📦</div>
            <h3 className="text-xl font-bold mb-2">For Donors</h3>
            <p className="text-gray-600">
              Restaurants, stores, and individuals can easily donate surplus
              food instead of wasting it.
            </p>
          </div>

          <div className="bg-white p-6 rounded-lg shadow-md text-center">
            <div className="text-4xl mb-4">🚴</div>
            <h3 className="text-xl font-bold mb-2">For Volunteers</h3>
            <p className="text-gray-600">
              Pick up donations and deliver them to those in need. Track your
              impact and earn rewards.
            </p>
          </div>

          <div className="bg-white p-6 rounded-lg shadow-md text-center">
            <div className="text-4xl mb-4">🌍</div>
            <h3 className="text-xl font-bold mb-2">Real Impact</h3>
            <p className="text-gray-600">
              Location-based matching ensures food reaches communities quickly
              before it spoils.
            </p>
          </div>
        </div>

        {/* Stats */}
        <div className="mt-16 bg-green-600 text-white rounded-lg p-8">
          <div className="grid md:grid-cols-3 gap-8 text-center">
            <div>
              <div className="text-4xl font-bold mb-2">1/3</div>
              <p className="text-green-100">of global food is wasted</p>
            </div>
            <div>
              <div className="text-4xl font-bold mb-2">828M</div>
              <p className="text-green-100">people face hunger worldwide</p>
            </div>
            <div>
              <div className="text-4xl font-bold mb-2">Together</div>
              <p className="text-green-100">we can make a difference</p>
            </div>
          </div>
        </div>

        {/* Technology Note */}
        <div className="mt-12 text-center text-sm text-gray-500">
          <p>Powered by Jakarta EE + Next.js</p>
        </div>
      </div>
    </div>
  );
}
