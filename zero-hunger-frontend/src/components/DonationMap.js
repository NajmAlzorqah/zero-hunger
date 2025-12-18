'use client';

import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import { useEffect, useState } from 'react';

// Fix for Leaflet marker icons in Next.js
const fixIcon = () => {
    if (typeof window !== 'undefined') {
        const L = require('leaflet');
        delete L.Icon.Default.prototype._getIconUrl;
        L.Icon.Default.mergeOptions({
            iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
            iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
        });
    }
};

export default function DonationMap({ donations = [], onClaim }) {
    const [mounted, setMounted] = useState(false);
    const defaultCenter = [30.0444, 31.2357]; // Cairo, Egypt

    useEffect(() => {
        fixIcon();
        setMounted(true);
    }, []);

    if (!mounted) {
        return (
            <div className="h-[400px] w-full bg-gray-200 rounded-lg flex items-center justify-center">
                Loading map...
            </div>
        );
    }

    return (
        <div className="h-[400px] w-full rounded-lg overflow-hidden shadow">
            <MapContainer center={defaultCenter} zoom={12} className="h-full w-full">
                <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution='&copy; OpenStreetMap contributors'
                />
                {donations.map((donation) => (
                    <Marker
                        key={donation.id}
                        position={[donation.latitude || 30.0444, donation.longitude || 31.2357]}
                    >
                        <Popup>
                            <div className="p-1">
                                <h3 className="font-bold text-sm">{donation.title}</h3>
                                <p className="text-xs text-gray-600">{donation.quantity_kg} kg</p>
                                {onClaim && (
                                    <button
                                        onClick={() => onClaim(donation.id)}
                                        className="mt-2 bg-green-600 text-white px-3 py-1 rounded text-xs"
                                    >
                                        Claim
                                    </button>
                                )}
                            </div>
                        </Popup>
                    </Marker>
                ))}
            </MapContainer>
        </div>
    );
}
