package model.unite_controlables;


import model.objets.Position;
import model.objets.UniteControlable;

import java.util.concurrent.ConcurrentHashMap;

public class SousMarin extends UniteControlable {
        public SousMarin(Position position) {
            super(5, position, 10, 15, 150);    //à modifier
        }



        @Override
        public ConcurrentHashMap<String, String> getAttributes() {
            ConcurrentHashMap<String, String> attributes = super.getAttributes();
            attributes.put("Type", "Sous-marin");   // à modifier ?
            return attributes;
        }



    }

