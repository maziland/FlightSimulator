package com.example.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
                ArrayList<String> simulator_data = new ArrayList<String>();

                while ((line = in.readLine()) != null) {
                    simulator_data.add(line);
                }
                
                int index = 0;
                while(simulator_data.size() > index)
                {
                    if (control.state == forward) {
                        control.state = "";
                        index = index + 1;
                    } 
                    else if (control.state == pause) {
                        while (control.state == pause) {

                        }
                    } else if (control.state == backward) {
                        index = index - 1;
                        control.state = "";
                    } 
                    else if (control.state == stop) {
                        out.close();
                        in.close();
                        fg.close();
                        control.state = "";
                        return;
                    } 
                    else if (control.state == tostart) {
                        index = 0;
                        control.state = "";
                        continue;
                    } 
                    else if (control.state == toend) {
                        control.state = "";
                        index = simulator_data.size();
                        continue;
                    }
                    out.println(simulator_data.get(index));
                    out.flush();
                    index = index + 1;
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
