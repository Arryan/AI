package cybercycles;

import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AI {

    /* Configuration */
    public final String ROOM = "VTC";
    public final String TEAM = "VTC2";
    
    //global game variables
    private int board[][]; //0 blank, 1-4 player x, 5 obstacle
    private int coords[][]; //0-3 player x -1, x or y coord (x:0, y:1)
    private int width;
    private int height;
    private int me;
    private String teamId[];
    private String dir[]; //contains direction of players 0-3, value of u, d, l, or r
    
    

    /* Déplacement de l'A.I. */
    public final char[] directions = {'u', 'l', 'd', 'r'};
    public char direction;

    Random random = new Random();

    /**
     * Fonction appelée en début de partie.
     *
     * @param config Configuration de la grille de jeu
     * @throws org.json.JSONException
     */
   
    public void start(JSONObject config) throws JSONException {
        JSONArray players = config.getJSONArray("players");
        this.coords = new int[4][2];
        this.teamId = new String[4];
        this.dir = new String[4];
        for (int i = 0; i < 4; i++ ){
            int id = players.getJSONObject(i).getInt("id");
            this.coords[id-1][0] = players.getJSONObject(i).getInt("x");
            this.coords[id-1][1] = players.getJSONObject(i).getInt("y");
            this.teamId[id-1] = players.getJSONObject(i).getString("team");
            this.dir[id-1] = players.getJSONObject(i).getString("direction");
        }
        this.width = config.getInt("w");
        this.height = config.getInt("h");
        this.me = Integer.parseInt(config.getString("me"));
 
        board = new int[width][height];
        JSONArray obstacles = config.getJSONArray("obstacles");
        for(int i = 0; i < obstacles.length(); i++){
            int x = obstacles.getJSONObject(i).getInt("x");
            int y = obstacles.getJSONObject(i).getInt("y");
            int w = obstacles.getJSONObject(i).getInt("w");
            int h = obstacles.getJSONObject(i).getInt("h");
            System.out.println(x + " " + y + " " + h + " " + w);
            for (int j = x; j < x + w; j++){
                for (int k = y; k < y + h; k++){
                    if (j < width && j >= 0 && k < height && k >= 0){
                        this.board[j][k] = 5;
                    }
                }
            }
        }
        for (int i = 0; i < coords.length; i++){
            board[coords[i][0]][coords[i][1]] = i+1;
        }
        
        
        printBoard();
        
        
        System.out.println("Joueurs : " + config.getJSONArray("players"));

        System.out.println("Obstacles : " + config.getJSONArray("obstacles"));

        System.out.print("Taille de la grille : ");
        System.out.println(config.getInt("w") + " x " + config.getInt("h"));

        System.out.println("Votre identifiant : " + config.getString("me"));
    }

    /**
     * Fonction appelée à chaque tour de jeu.
     *
     * @param prevMoves Mouvements précédents des joueurs
     * @return Mouvement à effectuer
     * @throws org.json.JSONException
     */
    public char next(JSONArray prevMoves) throws JSONException {
        String[] moves = {"", "", "", ""};
        System.out.println(prevMoves.length());
        System.out.print("IDS : ");
        
        
        for (int i = 0; i < prevMoves.length(); i++) {
            JSONObject prevMove = prevMoves.getJSONObject(i);
            System.out.print(prevMove.getString("id") + " ");
            moves[Integer.parseInt(prevMove.getString("id"))-1] = prevMove.getString("direction");
            //System.out.print(prevMove.getString("direction") + " ");
        }
        System.out.println("\nwoops " + moves.length);
        for (int i = 0; i < moves.length; i++){
            if (!moves.equals("")){
                if (moves[i].equals("u")){
                    this.coords[i][1]--;
                } else if (moves[i].equals("d")) {
                    this.coords[i][1]++;
                } else if (moves[i].equals("r")) {
                   this.coords[i][0]++;
                } else if (moves[i].equals("l")) {
                    this.coords[i][0]--;
                }
                int coordx = this.coords[i][0];
                int coordy = this.coords[i][1];
                if (coordx >= 0 && coordx < width && coordy >= 0 && coordy < height){
                    this.board[this.coords[i][0]][this.coords[i][1]] = i+1;
                }
            } else {
                this.coords[i][0] = -1;
                this.coords[i][1] = -1;
            }
        }
        //printBoard();
        System.out.print("\n");
        direction = this.nextMove();
        System.out.println("joueur: " + this.me);
        System.out.println("Mouvement choisi : " + direction);
        return direction;
    }

    /**
     * Fonction appelée en fin de partie.
     *
     * @param winnerID ID de l'équipe gagnante
     */
    public void end(String winnerID) {
        System.out.println("Équipe gagnante : " + winnerID);
    }
    
    
    private void printBoard(){
        System.out.println();
        for (int j = 0; j < height; j++){
            for (int i = 0; i < width; i++){
                if (board[i][j] == 0){
                    System.out.print("-");
                }else{
                    if (i == coords[me-1][0] && j == coords[me-1][1]){
                        System.out.print("*");
                    } else {
                    System.out.print(board[i][j]);
                    }
                }
            }
            System.out.print("\n");
            
        }
        System.out.print("coords of the head: " + coords[me-1][0] + ", " + coords[me-1][1]);
    } 
    
    private char nextMove(){
        char res = 'u';
        boolean[] possible = {false, false, false, false}; //u l d r
        int mex = coords[this.me-1][0];
        int mey = coords[this.me-1][1];
        int possibleMoves = 0;
        
        //up (-y)
        if (isPossible(mex, mey, mex, mey-1)){
            possible[0] = true;
            possibleMoves++;
        }
        //left -x
        if (isPossible(mex, mey, mex-1, mey)) {
            possible[1] = true;
            possibleMoves++;
        }
        //down +y
        if (isPossible(mex, mey, mex, mey + 1)){
            possible[2] = true;
            possibleMoves++;
        }
        //right +x
        if (isPossible(mex, mey, mex+1, mey)){
            possible[3] = true;
            possibleMoves++;
        }
        
        int[] areas = new int[] {-1, -1, -1, -1};
        int choice = 0;
        int max = -1;
        System.out.println("have to choose between " + possibleMoves + " moves");
        if (possibleMoves == 0){
            System.out.println("no possible moves");
            res = this.directions[random.nextInt(directions.length)];
        } else{
            
            int area = 0;
            for (int i = 0; i < 4; i++){
                if (possible[i]){
                    switch (directions[i]) {
                        case 'u':
                            areas[i] = calculateArea2(mex, mey-1);
                            break;
                        case 'l':
                            areas[i] = calculateArea2(mex - 1, mey);
                            break;
                        case 'd':
                            areas[i] = calculateArea2(mex, mey+ 1);
                            break;
                        case 'r':
                            areas[i] = calculateArea2(mex + 1, mey);
                            break;
                    }
                    if (areas[i] > max) {
                        max = areas[i];
                    }
                }
            }
            
            //direction
            boolean done = false;
            for (int i = 0; i < 4; i++){
                if (areas[i] == max){
                    if (directions[i] == direction){
                        choice = i;
                        done = true;
                    }
                }
            }
            
            while (!done){
                System.out.println("choosing random move...");
                choice = random.nextInt(directions.length);
                if (areas[choice] == max) {
                    done = true;
                }
            }
            res = this.directions[choice];
        }
        System.out.println("I chose to go " + res);
        System.out.println("max area: " + max);
        
        return res;
    }
    
    private boolean isPossible(int x1, int y1, int x2, int y2){
        if (x2 < 0 || y2 < 0 || x2 >= width || y2 >= height) {
            return false;
        } else if (board[x2][y2] == 0){
            boolean res = true;
            for (int i = 0; i < 4; i++){
                if (coords[i][0] >= 0 && i != this.me-1){
                    int otherx = coords[i][0];
                    int othery = coords[i][1];
                    //up -y
                    if (x2 == otherx && y2 == othery - 1){
                        res = false;
                    } 
                    // left -x    
                    if (x2 == otherx - 1 && y2 == othery){
                        res = false;
                    }
                    // down +y
                    if (x2 == otherx && y2 == othery + 1) {
                        res = false;
                    } 
                    // right +x
                    if(x2 == otherx + 1 && y2 == othery){
                        res = false;
                   }
                }
                return res;
            }
            return true;
        }
        return false;
    } 
    
    private int calculateArea(int mex, int mey){
        int area = 0;
        boolean[][] spaces = new boolean[width][height];
        int[][] positions; //nth position, x or y coord
        positions = new int[width * height][2];
        positions[0] = new int[]{mex, mey};
        int currentIndex = 0;
        boolean done = false;
        while (!done){
            //if up is possible
            //System.out.println("calculating area...");
    
            if (isPossible(mex, mey, mex, mey-1) && !spaces[mex][mey-1]){
                area++;
                spaces[mex][mey-1] = true;
                positions[area][0] = mex;
                positions[area][1] = mey - 1;
            } 
            //if left is possible
            if (isPossible(mex, mey, mex - 1, mey) && !spaces[mex-1][mey]){
                area++;
                spaces[mex-1][mey] = true;
                positions[area][0] = mex - 1;
                positions[area][1] = mey;
            }
            // is down is possible
            if (isPossible(mex, mey, mex, mey + 1) && !spaces[mex][mey+1]){
                area++;
                spaces[mex][mey+1] = true;
                positions[area][0] = mex;
                positions[area][1] = mey + 1;
            }
            // is right possible
            if (isPossible(mex, mey, mex + 1, mey) && !spaces[mex+1][mey]){
                area++;
                spaces[mex+1][mey] = true;
                positions[area][0] = mex + 1;
                positions[area][1] = mey;
            }
            currentIndex++;
            mex = positions[currentIndex][0];
            mey = positions[currentIndex][1];
            if (currentIndex == area + 1){
                done = true;
            }
             if (area > width*height/2 + 2){
                 done = true;
             }
        }
        
        return area;
    }
    
    private int calculateArea2(int mex, int mey){
        int area = -1;
        int maxArea = -1;
        if (board[mex][mey] == 0){
            board[mex][mey] = 6;
            //up -y
            if (isPossible(mex, mey, mex, mey - 1)){
                area = calculateArea(mex, mey-1);
                if (area > maxArea){
                    maxArea = area;
                }
            }
            // left -x
            if (isPossible(mex, mey, mex-1, mey)){
                area = calculateArea(mex- 1, mey);
                if (area > maxArea){
                    maxArea = area;
                }
            }
            // down +y
            if (isPossible(mex, mey, mex, mey + 1)){
                area = calculateArea(mex, mey+1);
                if (area > maxArea){
                    maxArea = area;
                }
            }

            //right + x
            if (isPossible(mex, mey, mex + 1, mey)){
                area = calculateArea(mex + 1, mey);
                if (area > maxArea){
                    maxArea = area;
                }
            }
            
            board[mex][mey] = 0;
        }
        
        
        
        return maxArea;
    }
    
    private void checkParallel() {
        int mex = coords[this.me-1][0];
        int mey = coords[this.me-1][1];
        
    }
}
