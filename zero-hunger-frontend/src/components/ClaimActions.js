'use client';

import { useState } from 'react';
import { claimAPI } from '@/lib/api';

export default function ClaimActions({ claim, onUpdate }) {
    const [pickupCode, setPickupCode] = useState('');
    const [showCodeInput, setShowCodeInput] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const handlePickup = async () => {
        if (pickupCode.length !== 6) {
            alert('Pickup code must be 6 digits');
            return;
        }
        setIsLoading(true);
        try {
            await claimAPI.markPickedUp(claim.id, pickupCode);
            alert('Marked as picked up!');
            if (onUpdate) onUpdate();
        } catch (error) {
            alert(error.response?.data?.message || 'Invalid pickup code');
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeliver = async () => {
        if (!confirm('Confirm delivery complete?')) return;
        setIsLoading(true);
        try {
            await claimAPI.markDelivered(claim.id);
            alert('Marked as delivered! Thank you!');
            if (onUpdate) onUpdate();
        } catch (error) {
            alert(error.response?.data?.message || 'Failed to mark as delivered');
        } finally {
            setIsLoading(false);
        }
    };

    const handleCancel = async () => {
        if (!confirm('Cancel this claim? The donation will become available again.')) return;
        setIsLoading(true);
        try {
            await claimAPI.cancel(claim.id);
            alert('Claim cancelled');
            if (onUpdate) onUpdate();
        } catch (error) {
            alert(error.response?.data?.message || 'Failed to cancel');
        } finally {
            setIsLoading(false);
        }
    };

    // Active state - needs pickup
    if (claim.status === 'active') {
        return (
            <div className="mt-3 space-y-2">
                {!showCodeInput ? (
                    <div className="flex gap-2">
                        <button
                            onClick={() => setShowCodeInput(true)}
                            className="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700"
                        >
                            📦 Mark Picked Up
                        </button>
                        <button
                            onClick={handleCancel}
                            disabled={isLoading}
                            className="bg-gray-500 text-white px-3 py-1 rounded text-sm hover:bg-gray-600"
                        >
                            Cancel
                        </button>
                    </div>
                ) : (
                    <div className="flex gap-2 items-center">
                        <input
                            type="text"
                            maxLength="6"
                            placeholder="6-digit code"
                            value={pickupCode}
                            onChange={(e) => setPickupCode(e.target.value.replace(/\D/g, ''))}
                            className="border rounded px-2 py-1 w-24 text-center font-mono"
                        />
                        <button
                            onClick={handlePickup}
                            disabled={isLoading}
                            className="bg-blue-600 text-white px-3 py-1 rounded text-sm"
                        >
                            {isLoading ? '...' : 'Verify'}
                        </button>
                        <button
                            onClick={() => setShowCodeInput(false)}
                            className="text-gray-500 text-sm"
                        >
                            Cancel
                        </button>
                    </div>
                )}
            </div>
        );
    }

    // Picked up - needs delivery
    if (claim.status === 'picked_up') {
        return (
            <div className="mt-3 flex gap-2">
                <button
                    onClick={handleDeliver}
                    disabled={isLoading}
                    className="bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700"
                >
                    {isLoading ? '...' : '✓ Mark Delivered'}
                </button>
                <button
                    onClick={handleCancel}
                    disabled={isLoading}
                    className="bg-gray-500 text-white px-3 py-1 rounded text-sm hover:bg-gray-600"
                >
                    Cancel
                </button>
            </div>
        );
    }

    // Delivered
    if (claim.status === 'delivered') {
        return (
            <div className="mt-3">
                <span className="text-green-600 font-semibold">✓ Delivered</span>
            </div>
        );
    }

    // Cancelled or other
    return (
        <div className="mt-3">
            <span className="text-gray-500">{claim.status}</span>
        </div>
    );
}
