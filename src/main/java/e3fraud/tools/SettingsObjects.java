/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package e3fraud.tools;

import com.hp.hpl.jena.rdf.model.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dan
 */
public class SettingsObjects {

    public static class GenerationSettings {

        private boolean generateHidden, generateNonOccurring, generateCollusion;
        private int maximumNumberOfColludingActors, numberOfHiddenTransfersPerExchange;
        private List<String> typesOfNonOccurringTransfers;

        public GenerationSettings(boolean generateHidden, boolean generateNonOccurring, boolean generateCollusion, int collusions, int numberOfHiddenTransfersPerExchange, List<String> typesOfNonOccurringTransfers) {
            this.generateHidden = generateHidden;
            this.generateNonOccurring = generateNonOccurring;
            this.generateCollusion = generateCollusion;
            this.maximumNumberOfColludingActors = collusions;
            this.numberOfHiddenTransfersPerExchange = numberOfHiddenTransfersPerExchange;
            this.typesOfNonOccurringTransfers = typesOfNonOccurringTransfers;
        }

        /**
         * Creates a advancedGenerationSettings object with default values
         */
        public GenerationSettings() {
            generateHidden = true;
            generateNonOccurring = true;
            generateCollusion = true;
            maximumNumberOfColludingActors = 2;
            numberOfHiddenTransfersPerExchange = 2;
            typesOfNonOccurringTransfers = new ArrayList<>();
            typesOfNonOccurringTransfers.add("MONEY");
        }

        public List<String> getTypesOfNonOccurringTransfers() {
            return typesOfNonOccurringTransfers;
        }

        public void setTypesOfNonOccurringTransfers(List<String> typesOfNonOccurringTransfers) {
            this.typesOfNonOccurringTransfers = typesOfNonOccurringTransfers;
        }

        public boolean isGenerateHidden() {
            return generateHidden;
        }

        public void setGenerateHidden(boolean generateHidden) {
            this.generateHidden = generateHidden;
        }

        public boolean isGenerateNonOccurring() {
            return generateNonOccurring;
        }

        public void setGenerateNonOccurring(boolean generateNonOccurring) {
            this.generateNonOccurring = generateNonOccurring;
        }

        public boolean isGenerateCollusion() {
            return generateCollusion;
        }

        public void setGenerateCollusion(boolean generateCollusion) {
            this.generateCollusion = generateCollusion;
        }

        public int getColludingActors() {
            return maximumNumberOfColludingActors;
        }

        public void setColludingActors(int colludingActors) {
            this.maximumNumberOfColludingActors = colludingActors;
        }

        public int getNumberOfHiddenTransfersPerExchange() {
            return numberOfHiddenTransfersPerExchange;
        }

        public void setNumberOfHiddenTransfersPerExchange(int numberOfHiddenTransfersPerExchange) {
            this.numberOfHiddenTransfersPerExchange = numberOfHiddenTransfersPerExchange;
        }

    }


    public static class SortingAndGroupingSettings {

        private int sortCriteria, groupingCriteria;
        private Resource actor;

        /**
         * @param sortCriteria 0 - do not sort, 1 - sort by loss first, 2- sort
         * by gain first, 3 - sort by loss of specific actor, 4 - sort by gain of specific actor
         * @param groupingCriteria 0 - group based on result (default), 1 - group based on collusion,
         * generated collusion groups
         */
        public SortingAndGroupingSettings(int sortCriteria, int groupingCriteria) {
            this.sortCriteria = sortCriteria;
            this.groupingCriteria = groupingCriteria;
        }

        public Resource getActor() {
            return actor;
        }

        public void setActor(Resource actor) {
            this.actor = actor;
        }

        /**
         * Creates a advancedGenerationSettings object with default values
         */
        public SortingAndGroupingSettings() {
            this.sortCriteria = 1;
            this.groupingCriteria = 0;
        }
        

        public int getSortCriteria() {
            return sortCriteria;
        }

        public void setSortCriteria(int sortCriteria) {
            this.sortCriteria = sortCriteria;
        }

        public int getGroupingCriteria() {
            return groupingCriteria;
        }

        public void setGroupingCriteria(int groupingCriteria) {
            this.groupingCriteria = groupingCriteria;
        }
    }

    public static class FilteringSettings {
        //TODO: use this object in the rest of the code

        private Double lossMin, lossMax, gainMin, gainMax;

        /**
         *
         * @param lossMin Filter by loss - minimum value
         * @param lossMax Filter by loss - maximum value
         * @param gainMin Filter by gain - minimum value
         * @param gainMax Filter by gain - maximum value
         */
        public FilteringSettings(Double lossMin, Double lossMax, Double gainMin, Double gainMax) {
            this.lossMin = lossMin;
            this.lossMax = lossMax;
            this.gainMin = gainMin;
            this.gainMax = gainMax;
        }

        /**
         * Creates a advancedGenerationSettings object with default values
         */
        public FilteringSettings() {
            this.lossMin = 0.0;
            this.gainMin = 0.0;
        }

        public void clearFilters(){
        this.lossMin = -Double.MAX_VALUE;
        this.lossMax = Double.MAX_VALUE;
        this.gainMin = -Double.MAX_VALUE;
        this.gainMax = Double.MAX_VALUE;
        }

                
        public Double getLossMin() {
            return lossMin;
        }

        public void setLossMin(Double lossMin) {
            this.lossMin = lossMin;
        }

        public Double getLossMax() {
            return lossMax;
        }

        public void setLossMax(Double lossMax) {
            this.lossMax = lossMax;
        }

        public Double getGainMin() {
            return gainMin;
        }

        public void setGainMin(Double gainMin) {
            this.gainMin = gainMin;
        }

        public Double getGainMax() {
            return gainMax;
        }

        public void setGainMax(Double gainMax) {
            this.gainMax = gainMax;
        }
    }
    
    public static class NCFSettings{
        public boolean vpValueObject;
        public boolean vpDirection;
        public boolean vpName;
        public boolean vtName;
        public boolean vtValueObject;
        public boolean GenActor;
        public boolean GenValueActivity;
        public boolean GenPerConstruct;
        public boolean viValueObjects;
        public boolean viName;
        public boolean createTransactions;

        public NCFSettings(boolean vpValueObject, boolean vpDirection, boolean vpName, boolean vtName, boolean vtValueObject, boolean GenActor, boolean GenValueActivity, boolean GenPerConstruct, boolean viValueObjects, boolean viName) {
            this.vpValueObject = vpValueObject;
            this.vpDirection = vpDirection;
            this.vpName = vpName;
            this.vtName = vtName;
            this.vtValueObject = vtValueObject;
            this.GenActor = GenActor;
            this.GenValueActivity = GenValueActivity;
            this.GenPerConstruct = GenPerConstruct;
            this.viValueObjects = viValueObjects;
            this.viName = viName;
        }

        /**
         *  instantiate with default settings
         */
        public NCFSettings() {
             this.vpValueObject = true;
            this.vpDirection = true;
            this.vpName = true;
            this.vtName = true;
            this.vtValueObject = true;
            this.GenActor = true;
            this.GenValueActivity = true;
            this.GenPerConstruct = true;
            this.viValueObjects = true;
            this.viName = true;
        }

        public boolean isVpValueObject() {
            return vpValueObject;
        }

        public void setVpValueObject(boolean vpValueObject) {
            this.vpValueObject = vpValueObject;
        }

        public boolean isVpDirection() {
            return vpDirection;
        }

        public void setVpDirection(boolean vpDirection) {
            this.vpDirection = vpDirection;
        }

        public boolean isVpName() {
            return vpName;
        }

        public void setVpName(boolean vpName) {
            this.vpName = vpName;
        }

        public boolean isVtName() {
            return vtName;
        }

        public void setVtName(boolean vtName) {
            this.vtName = vtName;
        }

        public boolean isVtValueObject() {
            return vtValueObject;
        }

        public void setVtValueObject(boolean vtValueObject) {
            this.vtValueObject = vtValueObject;
        }

        public boolean isGenActor() {
            return GenActor;
        }

        public void setGenActor(boolean GenActor) {
            this.GenActor = GenActor;
        }

        public boolean isGenValueActivity() {
            return GenValueActivity;
        }

        public void setGenValueActivity(boolean GenValueActivity) {
            this.GenValueActivity = GenValueActivity;
        }

        public boolean isGenPerConstruct() {
            return GenPerConstruct;
        }

        public void setGenPerConstruct(boolean GenPerConstruct) {
            this.GenPerConstruct = GenPerConstruct;
        }

        public boolean isViValueObjects() {
            return viValueObjects;
        }

        public void setViValueObjects(boolean viValueObjects) {
            this.viValueObjects = viValueObjects;
        }

        public boolean isViName() {
            return viName;
        }

        public void setViName(boolean viName) {
            this.viName = viName;
        }

        public boolean isCreateTransactions() {
            return createTransactions;
        }

        public void setCreateTransactions(boolean createTransactions) {
            this.createTransactions = createTransactions;
        }             
}  
}
