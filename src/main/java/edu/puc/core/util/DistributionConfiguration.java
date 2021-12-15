package edu.puc.core.util;

import java.util.Optional;

public class DistributionConfiguration {
    public final int process;
    public final int processes;

    // This will be equivalent to sequential i.e. process 0 does all the work.
    public static final DistributionConfiguration DEFAULT = new DistributionConfiguration(0, 1);

    public DistributionConfiguration(int process, int processes) {
        this.process = process;
        this.processes = processes;
    }

    static public Optional<DistributionConfiguration> parse(String str) throws DistributionConfigurationParsingException {
        if (str == null) {
            return Optional.empty();
        } else {
            String[] split = str.split(",");
            if (split.length != 2) {
                throw new DistributionConfigurationParsingException("Failed to split: " + str + " (expecting 'process,processes')");
            } else {
                int workerIndex;
                int totalWorkers;
                try {
                    workerIndex = Integer.parseInt(split[0]);
                    totalWorkers = Integer.parseInt(split[1]);
                } catch (NumberFormatException ex) {
                    throw new DistributionConfigurationParsingException("Failed to parse numbers: " + str);
                }
                return Optional.of(new DistributionConfiguration(workerIndex, totalWorkers));
            }
        }
    }

    public static class DistributionConfigurationParsingException extends Exception {
        public DistributionConfigurationParsingException(String msg){
            super(msg);
        }
    }
}
