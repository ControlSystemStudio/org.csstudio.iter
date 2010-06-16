package de.desy.language.snl.configuration.linux.configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.desy.language.snl.SNLConstants;
import de.desy.language.snl.compilerconfiguration.AbstractCompilerConfiguration;
import de.desy.language.snl.configurationservice.ICompilerOptionsService;

/*
 * Original compiler command
 * /usr/bin/g++ -o sncProgram  -L/scratch/EpicsR3.14.10/base/../unbundled/seq/lib/linux-x86 
 * -L/scratch/EpicsR3.14.10/base/lib/linux-x86 -Wl,
 * -rpath,/scratch/EpicsR3.14.10/base/../unbundled/seq/lib/linux-x86 -Wl,
 * -rpath,/scratch/EpicsR3.14.10/base/lib/linux-x86       -m32               sncProgram.o    
 * -lseq -lpv -lcas -lgdd -lasHost -ldbStaticHost -lregistryIoc -lca -lCom
 */
public class ApplicationCompilerConfiguration extends
		AbstractCompilerConfiguration {

	public ApplicationCompilerConfiguration(ICompilerOptionsService service) {
		super(service);
	}

	@Override
	public List<String> getCompilerParameters(String sourceFile,
			String targetFile) {
		List<String> result = new ArrayList<String>();
		result.add(getCompilerPath());
		result.add("-o");
		result.add(targetFile);
		result.add("-L"+getCompilerOptionService().getSeqFolder() + "/lib/linux-x86");
		result.add("-L/scratch/EpicsR3.14.10/DesyBase/lib/linux-x86");
		result.add("-L"+getCompilerOptionService().getEpicsFolder() + "/lib/linux-x86");
		result.add("-WL,-rpath,"+getCompilerOptionService().getSeqFolder() + "/lib/linux-x86");
		result.add("-WL,-rpath,/scratch/EpicsR3.14.10/DesyBase/lib/linux-x86");
		result.add("-WL,-rpath,"+getCompilerOptionService().getEpicsFolder() + "/lib/linux-x86");
		result.add("-m32");
		result.add(sourceFile);
		result.add("-lseq");
		result.add("-lpv");
		result.add("-liocLogClient");
		result.add("-lcas");
		result.add("-lgdd");
		result.add("-lasHost");
		result.add("-ldbStaticHost");
		result.add("-lregistryIoc");
		result.add("-lca");
		result.add("-lCom");
		return result;
	}

	@Override
	protected String getCompilerPath() {
		return getCompilerOptionService().getApplicationCompilerPath();
	}

	@Override
	public Pattern getErrorPattern() {
		return null;
	}

	@Override
	public String getSourceFileExtension() {
		return SNLConstants.O_FILE_EXTENSION.getValue();
	}

	@Override
	public String getSourceFolder() {
		return SNLConstants.BIN_FOLDER.getValue();
	}

	@Override
	public String getTargetFileExtension() {
		return SNLConstants.APPLICATION_FILE_EXTENSION.getValue();
	}

	@Override
	public String getTargetFolder() {
		return SNLConstants.BIN_FOLDER.getValue();
	}

}