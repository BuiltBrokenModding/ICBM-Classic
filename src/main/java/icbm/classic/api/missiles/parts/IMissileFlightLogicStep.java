package icbm.classic.api.missiles.parts;

/**
 * Notes that this logic is just one step of many that missile will perform
 * as part of it's flight path.
 */
public interface IMissileFlightLogicStep extends IMissileFlightLogic {

    /**
     * Gets the next step to run
     *
     * @return next step or null
     */
    IMissileFlightLogic getNextStep();

    /**
     * Sets the next step, it is up to the current flight logic when this is applied
     *
     * @param logic to add as next step
     * @return self to allow for easier chaining
     */
    IMissileFlightLogic setNextStep(IMissileFlightLogic logic);

    default IMissileFlightLogicStep addStep(IMissileFlightLogic logicStep) {
        if (getNextStep() == null) {
            setNextStep(logicStep);
        } else if (getNextStep() instanceof IMissileFlightLogicStep) {
            ((IMissileFlightLogicStep) getNextStep()).addStep(logicStep);
        } else {
            throw new IllegalArgumentException(this + "Next step is not an IMissileFlightLogicStep");
        }
        return this;
    }
}
