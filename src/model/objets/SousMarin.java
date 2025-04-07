package model.objets;



public class SousMarin extends UniteControlable {
        public SousMarin(Position position) {
            super(5, position, 10, 15, 150);    //à modifier
        }



        @Override
        public java.util.Map<String, String> getAttributes() {
            java.util.Map<String, String> attributes = super.getAttributes();
            attributes.put("Type", "Sous-marin");   // à modifier ?
            return attributes;
        }



    }

