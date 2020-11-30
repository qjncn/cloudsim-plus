package main;
import java.util.Arrays;
public class Qlearning {
    public static void main(String[] args) {
        double[][] Q = new double[][] {
            {-1,  0,  0, -1},
            {-1, -1, -1,  0},
            {-1, -1, -1,  0},
            {-1, -1, -1, -1}
        };

        int[][] graph = new int[][] {
            {0, 3, 2, 0},
            {0, 0, 0, 1},
            {0, 0, 0, 4},
            {0, 0, 0, 0}
        };

        double epsilon = 0.8;
        double alpha = 0.2;
        double gamma = 0.8;
        int MAX_EPISODES = 400; // 一般都通过设置最大迭代次数来控制训练轮数
        for(int episode = 0; episode < MAX_EPISODES; ++episode) {
            System.out.println("第"+episode+"轮训练...");
            int index = 0;
            while(index != 3) { // 到达目标状态，结束循环，进行下一轮训练
                int next;
                if(Math.random() < epsilon) next = max(Q[index]); // 通过 Q 表选择动作
                else next = randomNext(Q[index]); // 随机选择可行动作

                int reward =5 - graph[index][next]; // 奖励
                Q[index][next] = (1-alpha)*Q[index][next] + alpha*(reward+gamma*maxNextQ(Q[next]));
                index = next; // 更新状态
            }
        }
        System.out.println(Arrays.deepToString(Q));
    }

    private static int randomNext(double[] is) { // 蓄水池抽样，等概率选择流式数据
        int next = 0, n = 1;
        for(int i = 0; i < is.length; ++i) {
            if(is[i] >= 0 && Math.random() < 1.0/n++) next = i;
        }
        return next;
    }

    private static int max(double[] is) {
        int max = 0;
        for(int i = 1; i < is.length; ++i) {
            if(is[i] > is[max]) max = i;
        }
        return max;
    }

    private static double maxNextQ(double[] is) {
        double max = is[0];
        for(int i = 1; i < is.length; ++i) {
            if(is[i] > max) max = is[i];
        }
        return max;
    }

}
