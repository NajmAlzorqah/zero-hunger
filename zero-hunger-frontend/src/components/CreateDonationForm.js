"use client";

import { useState } from "react";
import { donationAPI } from "@/lib/api";

export default function CreateDonationForm({ onSuccess }) {
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    quantityKg: "",
    latitude: "30.0444", // Default to Cairo
    longitude: "31.2357",
    expiresAt: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await donationAPI.create(formData);
      alert("Donation created successfully!");
      setFormData({
        title: "",
        description: "",
        quantityKg: "",
        latitude: "30.0444",
        longitude: "31.2357",
        expiresAt: "",
      });
      setShowForm(false);
      if (onSuccess) onSuccess();
    } catch (error) {
      alert(error.response?.data?.message || "Failed to create donation");
    } finally {
      setIsLoading(false);
    }
  };

  if (!showForm) {
    return (
      <button
        onClick={() => setShowForm(true)}
        className="mb-4 bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
      >
        + Create New Donation
      </button>
    );
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="bg-white p-6 rounded shadow mb-6 space-y-4"
    >
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold">Create Donation</h2>
        <button
          type="button"
          onClick={() => setShowForm(false)}
          className="text-gray-500 hover:text-gray-700"
        >
          ✕
        </button>
      </div>

      <input
        type="text"
        placeholder="Title (e.g., Fresh Vegetables)"
        value={formData.title}
        onChange={(e) => setFormData({ ...formData, title: e.target.value })}
        className="w-full border rounded px-3 py-2"
        required
      />

      <textarea
        placeholder="Description (optional)"
        value={formData.description}
        onChange={(e) =>
          setFormData({ ...formData, description: e.target.value })
        }
        className="w-full border rounded px-3 py-2"
        rows="2"
      />

      <input
        type="number"
        step="0.1"
        placeholder="Quantity (kg)"
        value={formData.quantityKg}
        onChange={(e) =>
          setFormData({ ...formData, quantityKg: e.target.value })
        }
        className="w-full border rounded px-3 py-2"
        required
      />

      <div className="grid grid-cols-2 gap-4">
        <input
          type="number"
          step="0.0000001"
          placeholder="Latitude"
          value={formData.latitude}
          onChange={(e) =>
            setFormData({ ...formData, latitude: e.target.value })
          }
          className="border rounded px-3 py-2"
          required
        />
        <input
          type="number"
          step="0.0000001"
          placeholder="Longitude"
          value={formData.longitude}
          onChange={(e) =>
            setFormData({ ...formData, longitude: e.target.value })
          }
          className="border rounded px-3 py-2"
          required
        />
      </div>

      <div>
        <label className="block text-sm text-gray-600 mb-1">
          Expires at (optional)
        </label>
        <input
          type="datetime-local"
          value={formData.expiresAt}
          onChange={(e) =>
            setFormData({ ...formData, expiresAt: e.target.value })
          }
          className="w-full border rounded px-3 py-2"
        />
      </div>

      <button
        type="submit"
        disabled={isLoading}
        className="w-full bg-green-600 text-white py-2 rounded hover:bg-green-700 disabled:opacity-50"
      >
        {isLoading ? "Creating..." : "Create Donation"}
      </button>
    </form>
  );
}
