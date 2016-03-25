/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.virial.GUI.components;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import etomica.api.IAtomType;
import etomica.api.IPotential;
import etomica.api.IPotentialAtomic;
import etomica.atom.iterator.AtomsetIteratorBasisDependent;
import etomica.potential.PotentialGroup;
import etomica.virial.MCMoveClusterTorsionMulti;


public class CollectionPotentialAtomicUnlike implements ICollectionPotential {

	public int[] speciesIndex;
	
	private PotentialGroup potentialGroupInterNonBondedUnlike;
	
	private	HashMap<String[],IAtomType[]> hashMapAtomTypesUnlikePairs;

	private	HashMapPotentialNonBonded hashmapPotentialNonBonded;
	
	public CollectionPotentialAtomicUnlike(int index1, int index2){
		speciesIndex = new int[2];
		speciesIndex[0] = index1;
		speciesIndex[1] = index2;
		setHashMapPotentialNonBonded();
	}
	
	public void setHashMapAtomTypesUnlikePairs(HashMap<String[],IAtomType[]> hashMapAtomTypesUnlikePairs){
		this.hashMapAtomTypesUnlikePairs = hashMapAtomTypesUnlikePairs;
	}
	
	public HashMap<String[],IAtomType[]> getHashMapAtomTypesUnlikePairs(){
		return this.hashMapAtomTypesUnlikePairs;
	}
	
	
	public void setPotentialGroupInterNonBondedUnlike(PotentialGroup potentialGroupInterNonBondedUnlike){
		this.potentialGroupInterNonBondedUnlike = potentialGroupInterNonBondedUnlike;
	}
	
	public PotentialGroup getPotentialGroupInterNonBondedUnlike(){
		return this.potentialGroupInterNonBondedUnlike;
	}
		
	public void addToHashMapPotentialNonBonded(String[] hashKey, IPotential hashValuePotential){
		Map<String[], IPotential> nonBondedPotentialsMap = this.hashmapPotentialNonBonded.getHashMapPotentialNonBonded();
		Set<Entry<String[], IPotential>> nonBondedPotentialEntries = nonBondedPotentialsMap.entrySet();
		Iterator<Entry<String[], IPotential>> nonBondedPotentialsItr = nonBondedPotentialEntries.iterator();
		
		if(nonBondedPotentialsMap.size() == 0){
			this.hashmapPotentialNonBonded.getHashMapPotentialNonBonded().put(hashKey,hashValuePotential);
		}else{
			int tableIterationIndex = 0;
			while(nonBondedPotentialsItr.hasNext()){
				Entry nonBondedPotentialsEntry = (Entry) nonBondedPotentialsItr.next();
				String[] nonBondedPotentialsMapKey= (String[]) nonBondedPotentialsEntry.getKey();
				if(!(nonBondedPotentialsMapKey[0] == hashKey[0] && nonBondedPotentialsMapKey[1] == hashKey[1]) &&
						!(nonBondedPotentialsMapKey[0] == hashKey[1] && nonBondedPotentialsMapKey[1] == hashKey[0])){
					
					tableIterationIndex++;
				}
			}
			if(tableIterationIndex == nonBondedPotentialsMap.size()){
				this.hashmapPotentialNonBonded.getHashMapPotentialNonBonded().put(hashKey, hashValuePotential);
			}
			
		}
	}
	
	public HashMapPotentialNonBonded getHashMapPotentialNonBonded() {
		// TODO Auto-generated method stub
		return this.hashmapPotentialNonBonded;
	}
	
	public void setHashMapPotentialNonBonded() {
		// TODO Auto-generated method stub
		this.hashmapPotentialNonBonded = HashMapPotentialNonBonded.getInstance();
	}
	
	public int[] getSpeciesIndex() {
		return speciesIndex;
	}

	public void setSpeciesIndex(int[] speciesIndex) {
		this.speciesIndex = speciesIndex;
	}
	
	public int getSpeciesIndex(int index) {
		return speciesIndex[index];
	}

	public void setSpeciesIndex(int speciesIndex1, int speciesIndex2) {
		this.speciesIndex[0] = speciesIndex1;
		this.speciesIndex[1] = speciesIndex2;
	}
}
