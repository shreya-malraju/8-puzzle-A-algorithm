import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Expense_Eight_Puzzle_Problem {
    //variables
    private static Current_State starting_state, final_state;
    private static int number_of_nodes_generated = 0,number_of_nodes_popped = 0,maximum_size_of_fringe = 0,number_of_nodes_expanded = 0;
    private static PrintStream trace = null;

    enum Actions_supported {Start , Left, Right, Up, Down}
    ;

    enum Priority_Queue_Based_Solutions {UCS, GREEDY, A}
    ;
    enum Return {CutOff,Success,Failure};
    static class Current_State {
        public int location;
        public int[][] current_state;

        public Current_State(int[][] temp) {
            current_state = new int[3][];
            current_state[0] = temp[0].clone();
            current_state[1] = temp[1].clone();
            current_state[2] = temp[2].clone();
        }

        public Current_State(Current_State temp) {
            current_state = new int[3][];
            current_state[0] = temp.current_state[0].clone();
            current_state[1] = temp.current_state[1].clone();
            current_state[2] = temp.current_state[2].clone();
        }

        public Current_State() {
            current_state = new int[3][3];
        }

        @Override
        public String toString() {
            String string = "[";
            for (int p = 0; p < 3; p++) {
                string = string + "" + current_state[p][0] + current_state[p][1] + current_state[p][2];
                if (p != 2) {
                    string += "|";
                }
            }
            string = string + "]";
            return string;
        }

        @Override
        public int hashCode() {
            int hash = 0, x, y;
            for (x = 0; x < 3; x++) {
                for (y = 0; y < 3; y++) {
                    hash = hash * 10 + current_state[x][y];
                }
            }
            return hash;
        }

        @Override
        protected Object clone() {
            return new Current_State(this);
        }

        @Override
        public boolean equals(Object object) {
            int x, y;
            if (!(object instanceof Current_State)) {
                return false;
            }
            Current_State temp = (Current_State) object;
            for (x = 0; x < 3; x++) {
                for (y = 0; y < 3; y++) {
                    if (current_state[x][y] != temp.current_state[x][y]) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    static class Vertex {
        public Actions_supported action = Actions_supported.Start;
        public Current_State current_state = new Current_State();
        public Vertex root = null;
        public int dep = 0, fun = -1, gun = 0;

        @Override
        public String toString() {
            String string = "< State is " + current_state;
            string = string + "; action performed = {";
            if (action != Actions_supported.Start) {
                string += "Move " + current_state.location + " ";
            }
            string = string + action + "} ";

            string = string + "g(n) = " + gun + ", depth = " + dep + ", ";

            if (fun >= 0) {
                string = string + "f(n) = " + fun + ", ";
            }
            string = string + "Parent = {" + root + "} >";
            return string;
        }


    }

    private static Vertex create_vertex(Current_State temp) {
        Vertex vertex = new Vertex();
        vertex.current_state = (Current_State) temp.clone();
        return vertex;
    }

    private static boolean check_if_goal_state(Current_State temp) {

        return final_state.equals(temp);

    }

    private static void setup_cost_of_step(Vertex vertex1, Actions_supported action, Vertex vertex2) {
        int x, y;
        for (x = 0; x < 3; x++) {
            for (y = 0; y < 3; y++) {
                if (vertex1.current_state.current_state[x][y] == 0) {
                    vertex1.gun = vertex2.gun + vertex2.current_state.current_state[x][y];
                    return;
                }
            }
        }
    }

    private static HashMap<Actions_supported, Current_State> children(Current_State cs) {
        HashMap<Actions_supported, Current_State> child = new HashMap<>();
        int x, y;
        for (x = 0; x < 3; x++) {
            for (y = 0; y < 3; y++) {
                if (cs.current_state[x][y] == 0) {

                    if (y > 0) {
                        Current_State temp = (Current_State) cs.clone();
                        temp.current_state[x][y] = cs.current_state[x][y - 1];
                        temp.current_state[x][y - 1] = 0;
                        temp.location = temp.current_state[x][y];
                        child.put(Actions_supported.Right, temp);
                    }
                    if (x > 0) {
                        Current_State temp = (Current_State) cs.clone();
                        temp.current_state[x][y] = cs.current_state[x - 1][y];
                        temp.current_state[x - 1][y] = 0;
                        temp.location = temp.current_state[x][y];
                        child.put(Actions_supported.Down, temp);
                    }
                    if (y < 2) {
                        Current_State temp = (Current_State) cs.clone();
                        temp.current_state[x][y] = cs.current_state[x][y + 1];
                        temp.current_state[x][y + 1] = 0;
                        temp.location = temp.current_state[x][y];
                        child.put(Actions_supported.Left, temp);
                    }
                    if (x < 2) {
                        Current_State temp = (Current_State) cs.clone();
                        temp.current_state[x][y] = cs.current_state[x + 1][y];
                        temp.current_state[x + 1][y] = 0;
                        temp.location = temp.current_state[x][y];
                        child.put(Actions_supported.Up, temp);
                    }

                }
            }
        }
        return child;
    }

    private static ArrayList<Vertex> enlarge(Vertex vertex) {
        ArrayList<Vertex> child = new ArrayList<Vertex>();
        HashMap<Actions_supported, Current_State> result_of_action = children(vertex.current_state);
        for (Actions_supported a : result_of_action.keySet()) {
            Vertex temp = new Vertex();
            temp.root = vertex;
            temp.action = a;
            temp.current_state = result_of_action.get(a);
            setup_cost_of_step(temp, a, vertex);
            temp.dep = vertex.dep + 1;
            child.add(temp);
        }
        return child;

    }

    private static Vertex bfs_or_dfs(boolean bfs) {
        HashSet<Current_State> closed_set = new HashSet<>();
        LinkedList<Vertex> fringe_set = new LinkedList<>();
        fringe_set.add(create_vertex(starting_state));
        number_of_nodes_generated = number_of_nodes_generated + 1;
        maximum_size_of_fringe = 1;
        while (true) {
            if (fringe_set.isEmpty()) {
                return null;
            }
            Vertex vertex = null;
            if (bfs) {
                vertex = fringe_set.poll();
            } else {
                vertex = fringe_set.pollLast();
            }
            number_of_nodes_popped = number_of_nodes_popped + 1;
            if (check_if_goal_state(vertex.current_state)) {
                return vertex;
            }

            if (!closed_set.contains(vertex.current_state)) {
                closed_set.add(vertex.current_state);
                ArrayList<Vertex> next_node = enlarge(vertex);
                number_of_nodes_expanded = number_of_nodes_expanded + 1;
                number_of_nodes_generated = number_of_nodes_generated + next_node.size();
                fringe_set.addAll(next_node);
                if (fringe_set.size() > maximum_size_of_fringe) {
                    maximum_size_of_fringe = fringe_set.size();
                }
                if (trace != null) {
                    trace.println("Adding children to " + vertex);
                    trace.println("\t" + next_node.size() + " child generated");
                    trace.println("\tclosed_set: " + closed_set);
                    trace.println("\tfringe_set: " + fringe_set);
                }
            }
        }
    }

    private static void set_function(Vertex vertex, Priority_Queue_Based_Solutions process) {
        if (process == Priority_Queue_Based_Solutions.UCS) {
            vertex.fun = vertex.gun;
        } else if (process == Priority_Queue_Based_Solutions.GREEDY) {
            vertex.fun = admissible_heuristic_value(vertex);
        } else {
            vertex.fun = vertex.gun * admissible_heuristic_value(vertex);
        }
    }

    private static int admissible_heuristic_value(Vertex vertex) {
        int heuristic = 0, x, y;
        for (x = 0; x < 3; x++) {
            for (y = 0; y < 3; y++) {
                if (final_state.current_state[x][y] != vertex.current_state.current_state[x][y]) {
                    heuristic = heuristic + final_state.current_state[x][y];
                }
            }
        }
        return heuristic;
    }

    static class Order_of_queue implements Comparator<Vertex> {
        @Override
        public int compare(Vertex vertex1, Vertex vertex2) {
            return vertex1.fun - vertex2.fun;
        }
    }

    private static boolean check_if_cutoff=false;

    private static Vertex search_with_priority_queue(Priority_Queue_Based_Solutions process) {
        HashSet<Current_State> closed_set = new HashSet<>();
        PriorityQueue<Vertex> fringe_set = new PriorityQueue<>(new Order_of_queue());
        Vertex vertex = create_vertex(starting_state);
        set_function(vertex, process);
        fringe_set.add(vertex);
        number_of_nodes_generated = number_of_nodes_generated + 1;
        maximum_size_of_fringe = 1;
        while (true) {
            if (fringe_set.isEmpty()) {
                return null;
            }
            vertex = fringe_set.poll();
            number_of_nodes_popped = number_of_nodes_popped + 1;
            if (check_if_goal_state(vertex.current_state)) {
                return vertex;
            }

            if (!closed_set.contains(vertex.current_state)) {
                closed_set.add(vertex.current_state);
                ArrayList<Vertex> next_node = enlarge(vertex);
                number_of_nodes_generated = number_of_nodes_generated + next_node.size();
                number_of_nodes_expanded = number_of_nodes_expanded + 1;
                for (Vertex temp : next_node) {
                    set_function(temp, process);
                }
                fringe_set.addAll(next_node);
                if (fringe_set.size() > maximum_size_of_fringe) {
                    maximum_size_of_fringe = fringe_set.size();
                }
                if (trace != null) {
                    trace.println("Adding children to " + vertex);
                    trace.println("\t" + next_node.size() + " child generated");
                    trace.println("\tclosed_set: " + closed_set);
                    trace.println("\tfringe_set: " + fringe_set);
                }
            }


        }

    }

    private static Vertex recursive_depth_limited_search(Vertex vertex, int max_limit){
        check_if_cutoff=false;
        number_of_nodes_popped=number_of_nodes_popped+1;

        if(vertex.dep==max_limit){
            check_if_cutoff=true;
            return null;
        }
        if(check_if_goal_state(vertex.current_state)){
            return vertex;
        }

        ArrayList<Vertex> next_node = enlarge(vertex);
        number_of_nodes_generated = number_of_nodes_generated + next_node.size();
        number_of_nodes_expanded = number_of_nodes_expanded +1;

        if(trace != null){
            trace.println("Producing Children for " + vertex);
            trace.println("\t" + next_node.size() + "children generated");
            trace.println("\t Children " + next_node );
        }
        for(Vertex temp : next_node){
            Vertex v1 = recursive_depth_limited_search(temp,max_limit);
            if(v1 != null){
                return v1;
            }
        }
        return null;


    }

    private static Vertex depth_limited_search(int max_limit){
        if(trace != null){
            trace.println("Depth limited search with Limit=" + max_limit);
        }
        check_if_cutoff = false;
        Vertex vertex = create_vertex(starting_state);
        number_of_nodes_generated=number_of_nodes_generated+1;
        return recursive_depth_limited_search(vertex, max_limit);
    }

    private static Vertex iterative_deepening_search(){
        int temp=0;
        while(true){
            Vertex v1 = depth_limited_search(temp);
            if(v1 != null){
                return v1;
            }
            if(!check_if_cutoff){
                return null;
            }
            temp = temp +1;
        }
    }

    private static void solution_print(Vertex v1){
        if(v1.root !=null){
            solution_print(v1.root);
            System.out.println("\t Action  " + v1.current_state.location +"  "+ v1.action);
        }
    }

    public static void main(String[] a) {
        try {
            Scanner input_start = new Scanner(new File(a[0]));
            Scanner input_goal = new Scanner(new File(a[1]));
            int [][] start_array = new int[3][3];
            int [][] goal_array = new int[3][3];
            for(int x = 0; x < 3; x++) {
                for(int y = 0; y < 3; y++) {
                    start_array[x][y] = input_start.nextInt();
                    goal_array[x][y] = input_goal.nextInt();
                }
            }
            starting_state = new Current_State(start_array);
            final_state = new Current_State(goal_array);
            if(a[3].compareTo("true") == 0 && a.length == 4) {
                DateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy-hh_mm_ss_aaa");
                trace = new PrintStream(new File("trace-" + dateFormat.format(new Date()) + ".txt"));
                trace.print(" Given Command line args: (");
                for(String s : a) {
                    trace.print("\'" + s + "\', ");
                }
                trace.println(")");
            }
            int dep_limit = 0;
            Vertex vertex = null;
            String process = "a*";

            if(a.length > 2) {
                process = a[2];
            }

            if(process.compareTo("bfs") == 0) {
                vertex = bfs_or_dfs(true);
            }
            else if(process.compareTo("a*") == 0) {
                vertex = search_with_priority_queue(Priority_Queue_Based_Solutions.A);
            }
            else if(process.compareTo("dfs") == 0) {
                vertex = bfs_or_dfs(false);
            }
            else if(process.compareTo("greedy") == 0) {
                vertex = search_with_priority_queue(Priority_Queue_Based_Solutions.GREEDY);
            }
            else if(process.compareTo("ucs") == 0) {
                vertex = search_with_priority_queue(Priority_Queue_Based_Solutions.UCS);
            }
            else if(process.compareTo("ids") == 0) {
                vertex = iterative_deepening_search();
            }

            else if(process.compareTo("dls") == 0) {
                try (Scanner s = new Scanner(System.in)) {
                    System.out.println("Input depth limit: ");
                    dep_limit = s.nextInt();
                }
                vertex = depth_limited_search(dep_limit);
            }

            else {
                process = "a*";
                vertex = search_with_priority_queue(Priority_Queue_Based_Solutions.A);
            }

            if(trace != null) {
                trace.println("Process Choosen: "+process);
                if(process.compareTo("dls") == 0) {
                    System.out.println("Depth limit: " + dep_limit);
                }
            }
            String temp = "Number of Nodes Popped: " + number_of_nodes_popped + "\n"
                    + "Number of Nodes Generated: " + number_of_nodes_generated + "\n"
                    + "Number of Nodes Expanded: " + number_of_nodes_expanded + "\n"
                    + "Maximum Fringe Size: " + (maximum_size_of_fringe>0? ""+maximum_size_of_fringe:"-") + "\n";
            System.out.print(temp);

            if(trace != null) {
                trace.print(temp);
            }

            if(vertex == null) {
                if(process.compareTo("dls") == 0 && check_if_cutoff) {
                    System.out.println("No solution - Cutoff");
                }
                else {
                    System.out.println("No solution");
                }
            } else {
                System.out.println("Obtained Solution at a depth of " + vertex.dep
                        + " and final cost is " + vertex.gun +".");
                solution_print(vertex);
            }
        } catch (Exception e) {

        }
    }
}