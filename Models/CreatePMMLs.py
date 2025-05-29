from sklearn.ensemble import RandomForestRegressor
import ModelProcessor

agent_targets, heuristic_targets = ModelProcessor.load_target_data()

print("Generating heuristics model...")
ModelProcessor.export_pmml(heuristic_targets, RandomForestRegressor(random_state=69, n_estimators=150), '../Agents/src/agents/models/heuristics')

print("Generating agents model...")
ModelProcessor.export_pmml(agent_targets, RandomForestRegressor(random_state=69, n_estimators=150), '../Agents/src/agents/models/agents')
