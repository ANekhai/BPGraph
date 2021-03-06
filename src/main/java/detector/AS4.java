package detector;

import graphs.BPGraph;

import java.util.HashMap;

public class AS4 extends SubDetector{
    private String[] triangle = new String[3];
    private String[] pointingOut = new String[3];

    //represent vertices of distance 1, 2, 3 away from first node
    private String[] oneDeep = new String[3];
    private String[][] twoDeep = new String[3][3];
    private String[][] threeDeep = new String[3][3];

    public boolean AS4(BPGraph graph) {
        incident = graph.copyAvailability();
        valid = (HashMap<String, Boolean>) incident.clone();

        // Detect 5-3-5 type AS4
        detect535:
        for (String coreNode : graph.getNodes()) {
            if (!valid.get(coreNode)) {
                continue;
            }
            for (int color1 = 0; color1 < 2; ++color1) {
                //TODO: maybe change the name
                String coreAdj1 = graph.getFirstAdjacency(coreNode, color1);
                if (!valid.get(coreAdj1)) {
                    continue;
                }

                for (int color2 = color1 + 1; color2 < 3; ++color2) {
                    String coreAdj2 = graph.getFirstAdjacency(coreNode, color2);
                    if (!valid.get(coreAdj2)) {
                        continue;
                    }

                    int color3 = 3 - color1 - color2;

                    // TODO: When refactoring make a triangle detecting function separately

                    if (graph.getFirstAdjacency(coreAdj1, color3).equals(coreAdj2)) {
                        // Triangle Detected
                        triangle[color3] = coreNode;
                        triangle[color2] = coreAdj1;
                        triangle[color1] = coreAdj2;
                        updateVisitedVertices(valid, coreNode, coreAdj1, coreAdj2);

                        // Check if three points of the triangle point out
                        for (int i = 0; i < 3; ++i) {
                            pointingOut[i] = graph.getFirstAdjacency(triangle[i], i);
                            if (!valid.get(pointingOut[i])) {
                                continue detect535;
                            }
                        }

                        // DETECT 5-3-5 SUBGRAPHS
                        // Include 2-3, 2-4, 2-5, 2-6 subgraphs
                        for (int col1 = 0; col1 < 3; ++col1) {
                            int col2 = (col1 + 1) % 3, col3 = (col1 + 2) % 3;
                            String out12 = graph.getFirstAdjacency(pointingOut[col1], col2);
                            String out13 = graph.getFirstAdjacency(pointingOut[col1], col3);

                            if (!valid.get(out12) || !valid.get(out13)) {
                                continue;
                            }

                            if (graph.isConnected(out12, pointingOut[col2])
                                    && graph.isConnected(out13, pointingOut[col3])) {
                                // Found 5-3-5 subgraph
                                addVertices(pointingOut[col2], out12, pointingOut[col3], out13, triangle[col1],
                                        pointingOut[col1], triangle[col2], triangle[col2]);

                                updateVisitedVertices(valid, pointingOut[col2], out12, pointingOut[col3], out13,
                                        triangle[col1], pointingOut[col1], triangle[col2], triangle[col3]);

                                updateVisitedVertices(incident, pointingOut[col2], out12, pointingOut[col3], out13,
                                        triangle[col1], pointingOut[col1], triangle[col2], triangle[col3]);

                                continue detect535;
                            }


                        }
                        // Triangle found, but no AS4
                        continue detect535;

                    }
                }
            }
        }

        //loop to check for 3-3-3 or 3-3-other type subgraphs
        valid = (HashMap<String, Boolean>) incident.clone();

        detect333:
        for (String coreNode : graph.getNodes()) {
            if (!valid.get(coreNode)) {
                continue;
            }

            //TODO: this code assumes only one adjacency per node, not necessarily true in contracted bpgraph
            for (int color1 = 0; color1 < 3; ++color1) {
                oneDeep[color1] = graph.getFirstAdjacency(coreNode, color1);
                for (int color2 = 0; color2 < 3; ++color2) {
                    if (!valid.get(graph.getFirstAdjacency(graph.getFirstAdjacency(oneDeep[color1], color2), color1))
                            || !valid.get(graph.getFirstAdjacency(oneDeep[color1], color2))
                            || !valid.get(oneDeep[color1]) || color1 == color2) {
                        twoDeep[color1][color2] = null;
                        threeDeep[color1][color2] = null;

                    } else {
                        twoDeep[color1][color2] = graph.getFirstAdjacency(oneDeep[color1], color2);
                        threeDeep[color1][color2] = graph.getFirstAdjacency(twoDeep[color1][color2], color1);
                    }
                }
            }



            // Check for 3-3-3
            for (int color1 = 0; color1 < 3; ++color1) {
                if (threeDeep[0][color1] == null)
                    continue;

                for (int color2 = 0; color2 < 3; ++color2) {
                    if (threeDeep[1][color2] == null)
                        continue;

                    for (int color3 = 0; color3 < 3; ++color3) {
                        if (threeDeep[2][color3] == null)
                            continue;

                        //TODO: All above code matches
                        if (threeDeep[0][color1].equals(threeDeep[1][color2])
                                && threeDeep[0][color1].equals(threeDeep[2][color3])) {
                            boolean found = false;
                            //TODO: called co_core in original code ??? what does this mean???
                            String coCore = threeDeep[0][color1];

                            if (color1 != color2 && color1 != color3 && color2 != color3) {
                                found = true;
                            } else if (graph.isConnected(oneDeep[0], oneDeep[1])
                                    || graph.isConnected(oneDeep[0], oneDeep[2])
                                    || graph.isConnected(oneDeep[2], oneDeep[1])
                                    || graph.isConnected(twoDeep[0][color1], twoDeep[1][color2])
                                    || graph.isConnected(twoDeep[0][color1], twoDeep[2][color3])
                                    || graph.isConnected(twoDeep[2][color3], twoDeep[1][color2])) {
                                found = true;

                            }

                            if (found) {
                                addVertices(coreNode, coCore, oneDeep[0], twoDeep[0][color1], oneDeep[1],
                                        twoDeep[1][color2], oneDeep[2], twoDeep[2][color3]);

                                updateVisitedVertices(valid, coreNode, coCore, oneDeep[0], twoDeep[0][color1],
                                        oneDeep[1], twoDeep[1][color2], oneDeep[2], twoDeep[2][color3]);
                                updateVisitedVertices(incident, coreNode, coCore, oneDeep[0], twoDeep[0][color1],
                                        oneDeep[1], twoDeep[1][color2], oneDeep[2], twoDeep[2][color3]);
                                continue detect333;
                            }
                        }
                    }
                }
            }


            // Check for 3-3-other
            for (int c1 = 0; c1 < 2; ++c1) {
                for (int c2 = c1 + 1; c2 < 3; ++c2) {

                    for (int c1c = 0; c1c < 3; ++c1c) {
                        if (threeDeep[c1][c1c] == null) {
                            continue;
                        }

                        for (int c2c = 0; c2c < 3; ++c2c) {
                            if (threeDeep[c2][c2c] == null)
                                continue;

                            if (threeDeep[c1][c1c] == threeDeep[c2][c2c]) {


                                String coCore = threeDeep[c1][c1c];
                                String out1 = null, out2 = null;
                                boolean has33Other = false;
                                int c3 = 3 - c1 - c2;

                                if (valid.get(graph.getFirstAdjacency(coreNode, c3))
                                        && valid.get(graph.getFirstAdjacency(coCore, c3))) {
                                    out1 = graph.getFirstAdjacency(coreNode, c3);
                                    out2 = graph.getFirstAdjacency(coCore, c3);

                                    if (out1.equals(graph.getFirstAdjacency(graph.getFirstAdjacency(coreNode, c1), 3 - c1 - c1c))
                                            && out2.equals(graph.getFirstAdjacency(graph.getFirstAdjacency(coCore, c1), 3 - c1 - c1c))) {
                                        has33Other = true;
                                    } else if (out1.equals(graph.getFirstAdjacency(graph.getFirstAdjacency(coreNode, c2), 3 - c2 - c2c))
                                            && out2.equals(graph.getFirstAdjacency(graph.getFirstAdjacency(coCore, c2), 3 - c2 - c2c))) {
                                        has33Other = true;
                                    }

                                }
                                if (!has33Other) {
                                    String p1 = graph.getFirstAdjacency(coreNode, c1),
                                            p2 = graph.getFirstAdjacency(coreNode, c2);
                                    String cop1 = graph.getFirstAdjacency(coCore, c1),
                                            cop2 = graph.getFirstAdjacency(coCore, c2);

                                    if (c1 == c2c && c2 == c1c) {
                                        // Try to Detect (3-5)
                                        if (graph.getFirstAdjacency(p1, c3).equals(p2)) {
                                            out1 = graph.getFirstAdjacency(cop1, c3);
                                            out2 = graph.getFirstAdjacency(cop2, c3);
                                            if (valid.get(out1) && valid.get(out2) && graph.isConnected(out1, out2)) {
                                                has33Other = true;
                                            }

                                        } else if (graph.getFirstAdjacency(cop1, c3).equals(cop2)) {
                                            out1 = graph.getFirstAdjacency(p1, c3);
                                            out2 = graph.getFirstAdjacency(p2, c3);
                                            if (valid.get(out1) && valid.get(out2) && graph.isConnected(out1, out2)) {
                                                has33Other = true;
                                            }
                                        }

                                    }
                                    if (!has33Other) {
                                        // Try to detect (3-1) or (3-2)
                                        String p1e = graph.getFirstAdjacency(p1, 3 - c1 - c1c),
                                                cop2e = graph.getFirstAdjacency(cop2, 3 - c2 - c2c);
                                        String p2e = graph.getFirstAdjacency(p2, 3 - c2 - c2c),
                                                cop1e = graph.getFirstAdjacency(cop1, 3 - c1 - c1c);
                                        out1 = p1e;
                                        out2 = p2e;
                                        if (valid.get(out1) && valid.get(out2)
                                                && out1.equals(cop2e) && out2.equals(cop1e)) {
                                            has33Other = true;
                                        }
                                    }

                                }

                                if (has33Other) {
                                    addVertices(coreNode, coCore, out1, out2,
                                            oneDeep[c1], twoDeep[c1][c1c], oneDeep[c2], twoDeep[c2][c2c]);
                                    updateVisitedVertices(valid, coreNode, coCore, out1, out2,
                                            oneDeep[c1], twoDeep[c1][c1c], oneDeep[c2], twoDeep[c2][c2c]);
                                    updateVisitedVertices(incident, coreNode, coCore, out1, out2,
                                            oneDeep[c1], twoDeep[c1][c1c], oneDeep[c2], twoDeep[c2][c2c]);

                                    continue detect333;
                                }
                            }
                        }
                    }
                }
            }
        }


        if (foundSubgraphs.size() == 0) {
            return false;
        }
        this.numDetected = 1;
        return true;
    }

    @Override
    public void clean() {
        super.clean();
        triangle = new String[3];
        pointingOut = new String[3];
        oneDeep = new String[3];
        twoDeep = new String[3][3];
        threeDeep = new String[3][3];
    }

}
