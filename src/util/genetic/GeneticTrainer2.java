package util.genetic;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.genetic.mutatorV2.Mutator2;
import util.genetic.mutatorV2.MutatorV2;
import eval.expEvalV3.DefaultEvalWeights;
import eval.expEvalV3.EvalParameters;

public final class GeneticTrainer2 {
	private final static Comparator<GEntity> sortBestFirst = new Comparator<GEntity>() {
		public int compare(GEntity e1, GEntity e2) {
			if(e1 == null){
				return 1;
			} else if(e2 == null){
				return -1;
			}
			
			if(e1.score() > e2.score()){
				return -1;
			} else if(e1.score() < e2.score()){
				return 1;
			} else{
				return e1.totalGames() > e2.totalGames()? -1: 1;
			}
		}
	};
	
	private final static ByteBuffer b = ByteBuffer.allocate(1<<15);
	
	public static void main(String[] args) throws Exception{

		final int threads = 4;
		final long time = 30*1000;
		final int hashSize = 18;
		final int popSize = 40;
		final Mutator2 m = new MutatorV2();
		final double initialVariance = 10;
		final double initialMutationStdDev = 3; //standard deviation to apply during the initial mutating phase
		
		final File file = new File("genetic-results/genetic-results-mac-7");
		if(1==2&& file.exists()){
			System.out.println("log file already exists, exiting");
			System.exit(0);
		}
		final GeneticLogger log = new GeneticLogger(file);
		
		final GEntity[] population = new GEntity[popSize];
		final GameQueue q = new GameQueue(threads, time, hashSize);
		
		//generate initial population 
		for(int a = 0; a < population.length; a++){
			population[a] = new GEntity();
			population[a].p = DefaultEvalWeights.defaultEval();
			population[a].variance = initialVariance;
			if(a != 0) m.initialMutate(population[a].p, initialMutationStdDev);
			log.recordGEntity(population[a]);
			population[a].index = a;
		}
		
		for(int i = 0; ; i++){
			final Map<GEntity, GameQueue.Game[]> results = simulate(population, q);
			//log.recordIteration(i, population);
			cull(population, results, m);
			//recordNewEntities(culled, population, log);
			
			System.out.println("completed iteration "+i);
			System.out.println("-------------------------------");
			m.printStdDev(population);
			
			//print data for inspection while simulating
			System.out.println("-------------------------------");
			for(int a = 0; a < population.length; a++){
				System.out.println(a+"\t"+population[a]);
			}
			System.out.println("best id="+population[popSize-1].id);
			System.out.println(population[popSize-1].p);
		}
	}
	
	private static void recordNewEntities(final List<Integer> newEntities, final GEntity[] p,
			final GeneticLogger log) throws IOException{
		for(int index: newEntities){
			final GEntity temp = p[index];
			log.recordGEntity(temp);
		}
	}
	
	private static int max(final int i1, final int i2){
		return i1 > i2? i1: i2;
	}
	
	/** runs the simulation, accumulating a score for each entity*/
	public static Map<GEntity, GameQueue.Game[]> simulate(final GEntity[] population, final GameQueue q){
		Map<GEntity, GameQueue.Game[]> m = new HashMap<GEntity, GameQueue.Game[]>();
		for(int a = 0; a < population.length-1; a++){
			final int index = (int)(Math.random()*(population.length-a-1))+1+a;
			final GameQueue.Game[] temp = new GameQueue.Game[2];
			temp[0] = new GameQueue.Game(population[a], population[index]);
			temp[1] = new GameQueue.Game(population[index], population[a]);
			q.submit(temp[0]);
			q.submit(temp[1]);
			m.put(population[a], temp);
		}
		
		//wait for queued games to finish
		final int total = population.length*2;
		int prevCompleted = -1;
		int mod = max((int)(total*.1), 1);
		while(q.getOutstandingJobs() > 0){
			try{
				Thread.sleep(500);
			} catch(InterruptedException e){}
			if(total-q.getOutstandingJobs() != prevCompleted){
				prevCompleted = total-q.getOutstandingJobs();
				if((prevCompleted % mod) == 0) System.out.println("completed "+prevCompleted+" / "+total);
			}
		}
		System.out.println("-------------------------");
		
		return m;
	}
	
	/** culls bad solutions from population, returns list of culled indeces*/
	public static void cull(final GEntity[] population, final Map<GEntity, GameQueue.Game[]> m, final Mutator2 mutator){
		Set<Integer> exclude = new HashSet<Integer>();
		for(int a = 0; a < population.length-1; a++){
			if(!exclude.contains(a)){
				final GEntity e = population[a];
				final GameQueue.Game[] g = m.get(population[a]);
				final int score = g[0].getScore(e) + g[1].getScore(e);
				
				if(score >= 3){ //2 wins, or 1 win 1 draw
					//mutate and replace bad entry
					double mult = score == 3? 1: 2;
					final GEntity opp = g[0].getOpponent(e);
					final int index = opp.index;
					final GEntity temp = new GEntity();
					temp.index = index;
					temp.p = cloneParams(e.p);
					mutator.mutate(temp.p, population, index, mult);
					population[index] = temp;
					exclude.add(index);
				} else if(score == 2){ //2 draw
					//self mutate
					double mult = .5;
					final int index = e.index;
					final GEntity temp = new GEntity();
					temp.index = index;
					temp.p = cloneParams(e.p);
					mutator.mutate(temp.p, population, index, mult);
					population[index] = temp;
				}
			}
		}
	}
	
	public static EvalParameters cloneParams(final EvalParameters p){
		b.clear();
		p.write(b);
		b.rewind();
		final EvalParameters temp = new EvalParameters();
		temp.read(b);
		return temp;
	}
}
