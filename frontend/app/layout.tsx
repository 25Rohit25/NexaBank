import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import './globals.css';

const geistSans = Geist({
  variable: '--font-geist-sans',
  subsets: ['latin'],
});

const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
});

export const metadata: Metadata = {
  metadataBase: new URL(process.env.NEXT_PUBLIC_SITE_URL ?? 'http://localhost:3000'),
  title: 'Nexa Bank — Personal Banking',
  description:
    'Secure personal banking with live accounts, transfers, transaction history, and an AI banking assistant.',
  openGraph: {
    title: 'Nexa Bank — Banking, with clarity.',
    description: 'Secure personal banking with deterministic transfers and a grounded AI assistant.',
    images: [{ url: '/og.png', width: 1200, height: 630, alt: 'Nexa Bank — Banking, with clarity.' }],
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Nexa Bank — Banking, with clarity.',
    description: 'Secure personal banking with deterministic transfers and a grounded AI assistant.',
    images: ['/og.png'],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        {children}
      </body>
    </html>
  );
}
