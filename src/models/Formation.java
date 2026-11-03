package models;

import java.util.List;

import static models.Position.*;

public class Formation {
    private final String label;
    private final List<Position> slots;     // গোলকিপার বাদে ১০টা স্লট

    public Formation(String label, Position... slots) {
        if (slots.length != 10) {
            throw new IllegalArgumentException(label + ": মাঠের খেলোয়াড় ঠিক ১০ জন লাগবে, আছে " + slots.length);
        }
        this.label = label;
        this.slots = List.of(slots);
    }

    public static final Formation F_4312 = new Formation("4-3-1-2",
            LB, CB, CB, RB, DMF, CMF, CMF, AMF, CF, CF);

    public static final Formation F_4213 = new Formation("4-2-1-3",
            LB, CB, CB, RB, DMF, DMF, AMF, LWF, CF, RWF);

    public static final Formation F_4321 = new Formation("4-3-2-1",
            LB, CB, CB, RB, DMF, CMF, CMF, AMF, AMF, CF);

    public static final Formation F_3421 = new Formation("3-4-2-1",
            CB, CB, CB, LMF, CMF, CMF, RMF, AMF, AMF, CF);

    public static final Formation F_3412 = new Formation("3-4-1-2",
            CB, CB, CB, LMF, CMF, CMF, RMF, AMF, CF, CF);

    public static final Formation F_4231 = new Formation("4-2-3-1",
            LB, CB, CB, RB, DMF, CMF, LWF, AMF, RWF, CF);

    public static final Formation F_433 = new Formation("4-3-3",
            LB, CB, CB, RB, DMF, CMF, CMF, LWF, CF, RWF);

    public static final Formation F_442 = new Formation("4-4-2",
            LB, CB, CB, RB, LMF, CMF, CMF, RMF, CF, CF);

    public static final Formation F_352 = new Formation("3-5-2",
            CB, CB, CB, LMF, DMF, CMF, AMF, RMF, CF, CF);

    public String getLabel() {
        return label;
    }

    public List<Position> getSlots() {
        return slots;
    }

    @Override
    public String toString() {
        return label;
    }
}