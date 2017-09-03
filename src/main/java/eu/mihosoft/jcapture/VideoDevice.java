/*
 * Copyright 2017-2017 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If you use this software for scientific research then please cite the following publication(s):
 *
 * M. Hoffer, C. Poliwoda, & G. Wittum. (2013). Visual reflection library:
 * a framework for declarative GUI programming on the Java platform.
 * Computing and Visualization in Science, 2013, 16(4),
 * 181–192. http://doi.org/10.1007/s00791-014-0230-y
 */
package eu.mihosoft.jcapture;

import com.github.sarxos.webcam.Webcam;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Video device that can be captured via {@link JCapture}.
 */
public final class VideoDevice {

    private String name;
    private int width;
    private int height;
    private Webcam webcam;

    /**
     * Constructor.
     */
    private VideoDevice() {
        //
    }

    /**
     * Creates a new video device from webcam object.
     * @param webcam webcam object that shall be wrapped
     * @return video device
     */
    private static VideoDevice fromWebcam(Webcam webcam) {
        VideoDevice result = new VideoDevice();
        result.webcam = webcam;
        result.name = webcam.getName();
        result.width = (int)webcam.getViewSize().getWidth();
        result.height = (int)webcam.getViewSize().getHeight();

        return result;
    }

    /**
     * Returns all available devices.
     * @return available devices
     */
    public static List<VideoDevice> getAllDevices() {
        return Webcam.getWebcams().stream().
                map(VideoDevice::fromWebcam).collect(Collectors.toList());
    }

    /**
     * Returns the view height.
     * @return the view height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Returns the view width.
     * @return the view width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the name of this device.
     * @return the name of this device
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the view size.
     * @param width width
     * @param height height
     */
    public void setSize(int width, int height) {
        this.webcam.setViewSize(new Dimension(width, height));
    }

    /**
     * Returns the webcam.
     * @return the webcam
     */
    Webcam getWebcam() {
        return this.webcam;
    }

    /**
     * Opens this device.
     * @return {@code true} if successful; {@code false} otherwise
     */
    boolean open() {
        return webcam.open();
    }


    /**
     * Closes this device.
     * @return {@code true} if successful; {@code false} otherwise
     */
    boolean close() {
        return webcam.close();
    }
}
