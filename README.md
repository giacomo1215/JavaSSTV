# JavaSSTV Documentation

## 1. Project Overview
JavaSSTV is a Java-based library and application for encoding images into SSTV (Slow Scan Television) audio signals. SSTV is a method used primarily by amateur radio operators to transmit and receive static images via radio. This implementation focuses on converting images into audio signals that can be transmitted over radio frequencies using the `Scottie DX` protocol

## 2. Package Structure

The codebase is organized into the following structure:

* `src/com/sstv/` - Contains the core SSTV encoding components
* `Color.java` - Color representation and conversion
* `ImagePanel.java` - UI component for displaying image data
* `PreviewPanel.java` - UI component for image preview
* `Program.java` - Main entry point for the application
* `Sound.java` - Sound generation and manipulation
* `SSTVEncoder.java` - Basic SSTV encoding utilities
* `SSTVImageEncoder.java` - Complete image to SSTV audio encoder
* `tests/` - Contains test cases
* `SoundOutput.java` - Test for audio output functionality

## 3. Core Components

### 3.1 Sound Engine

The Sound class is the foundation of the audio generation system. It provides functionality to:

* Generate pure sine wave tones at specified frequencies
* Create FSK (Frequency Shift Keying) modulated signals
* Render audio data to buffers
* Play audio through the system's audio output
### 3.2 SSTV Encoding

Two key classes handle SSTV encoding:

* `SSTVEncoder` - Provides primitive SSTV encoding functions
* `SSTVImageEncoder` - Implements a complete image-to-SSTV encoding system

### 3.3 UI Components
For visual representation:
* `ImagePanel` - Displays image data with customizable pixel size
* `PreviewPanel` - Provides a scalable preview of images being processed

## 4. Class Documentation

### 4.1 Color
Custom color implementation with RGB components and conversion utilities.

**Key Features:**

* RGB color representation with bounds checking (0-255)
* Conversion to hexadecimal and decimal representations
* RGB to YCbCr color space conversion (important for SSTV encoding)

**Methods:**

* `getR()`, `getG()`, `getB()` - Get individual color components
* `getHex()` - Get color as hexadecimal string
* `getDecimal()` - Get color as decimal integer
* `toYCbCr()` - Convert to YCbCr color space

### 4.2 Sound

Core audio generation class for creating tones and signals.

**Key Features:**

* Static and variable frequency tone generation
* FSK (Frequency Shift Keying) signal generation
* Audio buffer management
* Direct audio output through system speakers

**Methods:**

* `playTone()` - Play a sine wave tone
* `playFSK()` - Play a frequency shift keyed signal
* `playScanLine()` - Play a scan line with varying frequencies
* `renderToBuffer()` - Render a tone to a byte array buffer
* `playBuffer()` - Play audio from a buffer

**Constants:**
* `SAMPLE_RATE` - 44100 Hz (CD quality audio)

### 4.3 SSTVEncoder
Utility class with basic SSTV encoding functions.

**Methods:**

* `playSyncPulse()` - Play a sync pulse (1200 Hz)
* `playPorch()` - Play a porch signal (1500 Hz)
* `playScanLine()` - Play a scan line with specified frequencies
* `rgbToFrequency()` - Convert RGB color component to corresponding SSTV frequency

### 4.4 SSTVImageEncoder
Advanced encoder that converts image files to SSTV audio signals.

**Key Features:**

* Complete image-to-SSTV conversion
* Phase-continuous tone generation
* RGB color channel separation and encoding
* VIS (Vertical Interval Signaling) header generation

**Methods:**

* `encodeImage()` - Convert an image file to SSTV audio and play it
* `renderLine()` - Encode a single scan line from the image
* `renderColor()` - Encode a specific color component of a scan line
* `renderTone()` - Generate a continuous tone with optional tapering
* `renderSweep()` - Generate a frequency sweep for image data
* `renderFSK()` - Generate FSK signals for control data
* `renderSilence()` - Generate a silent period

### 4.5 ImagePanel
UI component for displaying image data with customizable pixel size.

**Key Features:**
* Custom rendering of color matrix data
* Configurable pixel size for display

### 4.6 PreviewPanel
UI component for displaying image preview with scaling.

**Key Features:**

* Scalable image preview
* Individual pixel setting and rendering
* Access to underlying image data

**Methods:**

* `setPixel()` - Set a specific pixel's color
* `resetImage()` - Clear the image display
* `getImage()` - Get the underlying BufferedImage


## 5. Technical Details

### 5.1 Audio Specifications
* Sample Rate: 44100 Hz (CD quality)
* Bit Depth: 16-bit PCM
* Channels: Mono
* Encoding: Little-endian signed PCM

## 5.2 SSTV Protocol Implementation

The encoder implements a typical SSTV protocol with:
* VIS header (8-bit code)
* Vertical sync pulses (1200 Hz)
* Horizontal sync pulses for each line
* Porch signals (1500 Hz)
* RGB color channel separation
* Frequency mapping: 1500-2300 Hz for brightness values

## 5.3 Signal Improvements
Phase continuity maintained across all tones
Tapering (5ms windows) used to reduce audio artifacts
Smooth transitions between frequencies during scan lines

## 6. Usage Examples
## 6.1 Basic Usage
## 6.2 Custom Sound Generation
## 6.3 Manual SSTV Signal Generation
