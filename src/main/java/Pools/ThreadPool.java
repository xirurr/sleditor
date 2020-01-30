package Pools;

import lombok.Data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Data
public class ThreadPool {
    static ExecutorService executeIt = Executors.newFixedThreadPool(8);
}
