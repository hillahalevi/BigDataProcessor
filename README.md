# BigDataProcessor
multi threaded program to enable fast processing of large csv files.
2 implementations :

*Spring batch with multiple threads :
   - Runner class - BatchRunner
        * using spring batch job mechanism plus parallel  processing for  better performance
        * results : 1000000 entries processed in 2s413ms (chunk size -1000, pool limit = 20)
        * results : 100 entries processed in 52ms (chunk size -20, pool limit = 10)

*Producer consumer paradigm
   - Runner class - ProducerConsumerRunner
     * the producer thread  reads constantly lines and adds them to q1
     * the consumer threads take input from q1 - process them and if needed adds to q2
     * whenever q2 reached max capacity - write all content to file and then clear q2
     * results : 1000000 entries processed in 15778ms  (chunk size -1000, pool limit = 20)
     * results : 100 entries processed in 36ms  (chunk size -20, pool limit = 10)



change of properties can be done in the Constants class.

each run produce the input file (testFile.csv) and the processing results are written to outputData.csv