import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        SlopeOne slopeOne = new SlopeOne();
        slopeOne.test();
        System.out.println(slopeOne.predict(3,1));
        slopeOne.predictBest(3, 2);
    }

//    public static void main(String[] args) {
//        Spark.get("/", new Route() {
//            public Object handle(Request request,
//                                 Response response) {
//                String todolist = "todo";
//                // XXX write your code here XXX
//                return "<html><body>"
//                        + "<form method=\"get\" action=\"./\">"
//                        + "<input type=\"text\" name=\"input\""
//                        + " placeholder=\"What do you want to do?\"><br>"
//                        + "<input type=\"submit\" value=\"Add item\">"
//                        + "</form><hr>"
//                        + (todolist == null ? "" : todolist)
//                        + "</body><html>";
//            }
//        });
//    }
}