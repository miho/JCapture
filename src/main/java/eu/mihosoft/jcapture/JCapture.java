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

import eu.mihosoft.vcollections.VList;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Captures video devices.
 */
public final class JCapture {

    private VideoDevice videoDevice;

    private boolean stopCamera = false;

    private BooleanProperty runningProperty = new SimpleBooleanProperty();

    private ObjectProperty<Image> imageProperty;

    private final VList<AWTImageListener> awtListeners =
            VList.newInstance(new ArrayList<>());

    private final VList<JFXImageListener> fxListeners =
            VList.newInstance(new ArrayList<>());

    /**
     * Creates a new capture object using the specified video device.
     *
     * @param device video device to use
     */
    public JCapture(VideoDevice device) {
        this.videoDevice = device;
    }

    /**
     * Starts capturing.
     */
    public void startCapture() {

        if(runningProperty.get()) {
            throw new RuntimeException("Already capturing '"
                    + videoDevice.getName() + "'Stop capturing before calling this method.");
        }

        if(!videoDevice.open()) {
            throw new RuntimeException("Cannot open device '" + videoDevice.getName() + "'.");
        }

        runningProperty.set(true);
        stopCamera = false;

        System.out.println("here 0");

        Runnable task = new Runnable() {

            @Override
            public void run() {

                final AtomicReference<WritableImage> ref = new AtomicReference<>();
                BufferedImage img;

                Exception ex = null;

                while (!stopCamera) {
                    try {
                        if ((img = videoDevice.getWebcam().getImage()) != null) {

                            if(!fxListeners.isEmpty() || imageProperty!=null) {
                                ref.set(SwingFXUtils.toFXImage(img, ref.get()));
                                img.flush();

                                Platform.runLater(() -> {
                                    WritableImage fxImage = ref.get();
                                    if(imageProperty!=null) {
                                        imageProperty.set(fxImage);
                                    }
                                    fxListeners.forEach(fxL -> fxL.onNewImage(fxImage));
                                });
                            }

                            if(!awtListeners.isEmpty()) {
                                final BufferedImage finalImg = img;
                                awtListeners.forEach(awtL -> awtL.onNewImage(finalImg));
                            }

                        }
                    } catch (Exception e) {
                        ex = e;
                        e.printStackTrace();
                    }
                } // end while !stopCamera

                runningProperty.set(false);

                if(ex!=null) {
                    throw new RuntimeException("Capturing device '" + videoDevice.getName() + "' failed.", ex);
                }

                if(!videoDevice.close()) {
                    throw new RuntimeException("Cannot close device '" + videoDevice.getName() + "'.");
                }
            }
        }; // end task

        Thread th = new Thread(task);
        th.setDaemon(false);
        th.start();
    }

    /**
     * Stops capturing.
     */
    public void setStopCamera() {
        stopCamera = true;
    }

    /**
     * Indicates whether currently capturing.
     * @return {@code true} if currently running; {@code false} otherwise
     */
    public boolean isRunning() {
        return runningProperty().get();
    }

    /**
     * Returns the 'running' property.
     * @return the 'running' property
     */
    public ReadOnlyBooleanProperty runningProperty() {
        return this.runningProperty;
    }

    /**
     * Adds an AWT image listener.
     * @param l listener to add
     * @return subscription
     */
    public Subscription addAWTListener(AWTImageListener l) {
        awtListeners.add(l);
        return () -> awtListeners.remove(l);
    }

    /**
     * Adds a JavaFX image listener.
     * @param l listener to add
     * @return subscription
     */
    public Subscription addJFXListener(JFXImageListener l) {
        fxListeners.add(l);
        return () -> fxListeners.remove(l);
    }

    /**
     * Returns the JavaFX image property.
     * @return the JavaFX image property
     */
    public ReadOnlyObjectProperty<Image> jfxImageProperty() {
        if(imageProperty==null) {
            imageProperty = new SimpleObjectProperty<>();
        }

        return imageProperty;
    }
}
