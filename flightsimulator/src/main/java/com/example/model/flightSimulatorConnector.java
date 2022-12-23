package com.example.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class flightSimulatorConnector {
    public final Control control = new Control();
    public final String forward = "FORWARD";
    public final String backward = "BACKWARD";
    public final String pause = "PAUSE";
    public final String run = "RUN";
    public final String stop = "STOP";
    public final String toend = "END";
    public final String tostart = "START";

    public class Control {
        public volatile String state = "";
    }

    private String simulator_ip = "localhost";
    private int simulator_port = 5400;
    private int default_delay = 1;
    final String csv_config_path = "flightsimulator/src/main/config/flight.csv";

    class FlightConnector implements Runnable {
        @Override
        public void run() {
            Socket fg;
            try {
                fg = new Socket(simulator_ip, simulator_port);
            } catch (UnknownHostException e) {
                System.out.println(e.toString());
                return;
            } catch (IOException e) {
                System.out.println(e.toString());
                return;
            } catch (Exception e) {
                return;
            }
            BufferedReader in;
            PrintWriter out;
            try {
                in = new BufferedReader(new FileReader(csv_config_path));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                return;
            }
            try {
                out = new PrintWriter(fg.getOutputStream());
            } catch (IOException e1) {
                e1.printStackTrace();
                return;
            }

            String line;
            try {
                while ((line = in.readLine()) != null) {
                    if (control.state == forward) {
                        control.state = "";
                        Thread.sleep(default_delay * 10);
                        continue;
                    } else if (control.state == pause) {
                        while (control.state == pause) {

                        }
                    } else if (control.state == backward) {
                        // TODO
                    } else if (control.state == stop) {
                        out.close();
                        in.close();
                        fg.close();
                        control.state = "";
                        return;
                    } else if (control.state == tostart) {
                        in = new BufferedReader(new FileReader(csv_config_path));
                        continue;
                    } else if (control.state == toend) {
                        String last_line = line;
                        while ((line = in.readLine()) != null) {
                            last_line = line;
                        }
                        line = last_line;
                    }
                    out.println(line);
                    out.flush();
                    Thread.sleep(default_delay * 10);
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