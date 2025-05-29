package agents.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ModelEvaluatorBuilder;
import org.jpmml.model.JAXBUtil;
import org.jpmml.model.SAXUtil;
import org.jpmml.model.filters.ImportFilter;
import org.xml.sax.SAXException;

import game.Game;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import ludemeplexDetection.GetLudemeInfo;
import main.grammar.LudemeInfo;
import other.concept.Concept;
import other.concept.ConceptComputationType;
import other.concept.ConceptDataType;

public class ModelUtils {

	public static Evaluator agentEvaluator;
	static {
		try {
			agentEvaluator = modelEvaluator("/agents/models/agents.pmml");
		} catch (JAXBException | IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
	}

	public static Evaluator heuristicEvaluator;
	static {
		try {
			heuristicEvaluator = modelEvaluator("/agents/models/heuristics.pmml");
		} catch (JAXBException | IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Double> getInputMap(Game game) {

		Map<String, Double> inputMap = new LinkedHashMap<String, Double>();

		// concepts
		for (int i = 0; i < Concept.values().length; i++) {
			final Concept concept = Concept.values()[i];
			if (concept.computationType().equals(ConceptComputationType.Compilation)) {
				if (concept.dataType().equals(ConceptDataType.BooleanData)) {
					if (game.booleanConcepts().get(concept.id()))
						inputMap.put(concept.name(), Double.valueOf(1.0));
					else
						inputMap.put(concept.name(), Double.valueOf(0.0));
				} else if (concept.dataType().equals(ConceptDataType.DoubleData)
						|| concept.dataType().equals(ConceptDataType.IntegerData)) {
					inputMap.put(concept.name(),
							Double.valueOf(game.nonBooleanConcepts().get(Integer.valueOf(concept.id()))));
				} else {
					System.out.println("ERROR, the following concept has an invalid type " + concept.toString());
				}
			}
		}

		// ludemes
		final Map<LudemeInfo, Integer> ludemesInGame = game.description().callTree().analysisFormat(0,
				GetLudemeInfo.getLudemeInfo());
		for (LudemeInfo ludemeInfo : ludemesInGame.keySet()) {
			if (ludemesInGame.get(ludemeInfo) > 0)
				inputMap.put(ludemeInfo.symbol().path(), 1d);
		}

		return inputMap;
	}

	public static Evaluator modelEvaluator(String fileName)
			throws JAXBException, FileNotFoundException, IOException, ParserConfigurationException, SAXException {

		Unmarshaller unmarshaller = JAXBUtil.createUnmarshaller();
		Evaluator evaluator = null;

		try (InputStream is = ModelUtils.class.getResourceAsStream(fileName)) {
			Source source = SAXUtil.createFilteredSource(is, new ImportFilter());

			PMML pmml = (PMML) unmarshaller.unmarshal(source);
			ModelEvaluatorBuilder modelEvaluatorBuilder = new ModelEvaluatorBuilder(pmml);

			evaluator = modelEvaluatorBuilder.build();
		}
		return evaluator;
	}
}
