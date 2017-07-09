package aplicacao.mineracao;

import java.util.Objects;

import aplicacao.data.TripCustom;

public class WrapperTripCustom {
	private TripCustom tc;

	public WrapperTripCustom(TripCustom tp) {
		this.tc = tp;
	}

	public TripCustom unwrap() {
		return this.tc;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrapperTripCustom that = (WrapperTripCustom) o;
        return Objects.equals(tc.getId(), that.tc.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(tc.getId());
    }	
	
}
