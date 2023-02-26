package com.example.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.util.ArrayList;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class FlightSimulatorConnector {
    public final Control control = new Control();
    public final String forward = "FORWARD";
    public final String backward = "BACKWARD";
    public final String pause = "PAUSE";
    public final String run = "RUN";
    public final String stop = "STOP";
    public final String toend = "END";
    public final String tostart = "START";

    public class Control {
        public volatile String state = ""; // control the state of the simulator
        public volatile int delay = 1; // control the speed of the simulator
        public int index = 0;
        public IntegerProperty currentTimeStep;

        public Control() {
            this.currentTimeStep = new SimpleIntegerProperty();
            this.currentTimeStep.set(0);
        }

    }

    private String simulator_ip = "localhost";
    private int simulator_port = 5400;
    final String csv_config_path = FileSystems.getDefault().getPath(".").toString() + "\\" + "flight.csv";

    class FlightConnector implements Runnable {
        @Override
        public void run() {
            Socket fg;
            try {
                fg = new Socket(simulator_ip, simulator_port);
            } catch (Exception e) {
                control.state = "";
                return;
            }

            BufferedReader in;
            PrintWriter out;
            try {
                in = new BufferedReader(new FileReader(csv_config_path));
            } catch (FileNotFoundException e1) {
                try {
                    control.state = "";
                    fg.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    control.state = "";
                }
                e1.printStackTrace();
                control.state = "";
                return;
            }
            try {
                out = new PrintWriter(fg.getOutputStream());
            } catch (IOException e1) {
                try {
                    fg.close();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    control.state = "";
                }
                e1.printStackTrace();
                control.state = "";
                return;
            }

            String line;
            try {
                ArrayList<String> simulator_data = new ArrayList<String>();

                while ((line = in.readLine()) != null) {
                    simulator_data.add(line);
                }
                simulator_data.remove(0);
                System.out.println("total rows:" + simulator_data.size());

                while (simulator_data.size() > control.currentTimeStep.get()) {
                    if (control.state == forward) {
                        control.state = "";
                    } else if (control.state == pause) {
                        while (control.state == pause) {

                        }
                    } else if (control.state == backward) {
                        control.state = "";
                    } else if (control.state == stop) {
                        out.close();
                        in.close();
                        fg.close();
                        control.state = "";
                        control.currentTimeStep.set(1);
                        return;
                    } else if (control.state == tostart) {
                        control.currentTimeStep.set(1);
                        control.state = "";
                        continue;
                    } else if (control.state == toend) {
                        control.state = "";
                        control.currentTimeStep.set(simulator_data.size());
                        continue;
                    }
                    out.println(simulator_data.get(control.currentTimeStep.get()));
                    out.flush();
                    control.currentTimeStep.set(control.currentTimeStep.get() + 1);
                    Thread.sleep(50 / control.delay);
                }
                out.close();
                in.close();
                fg.close();
                control.state = "";
            } catch (IOException e) {
                System.out.println(e.toString());
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        }
    }

    public void executeFlight() {
        FlightConnector flight = new FlightConnector();
        new Thread(flight).start();
    }
}
