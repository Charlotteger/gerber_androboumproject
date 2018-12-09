package com.example.android.androboumproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class OtherUserActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user);
        //chargement de la liste d'utilisateur
        final List<Profil> userList = new ArrayList<>();
        final MyPagerAdapter adapter = new MyPagerAdapter(this, userList);
        final ViewPager pager = (ViewPager) findViewById(R.id.pager);


        // on obtient l'intent utilisé pour l'appel
        Intent intent = getIntent();
        // on va chercher la valeur du paramètre position, et on renvoie zéro si ce paramètre n'est pas positionné (ce qui ne devrait pas arriver dans notre cas).
        final int position = intent.getIntExtra("position", 0);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    userList.add(child.getValue(Profil.class));
                }
                adapter.notifyDataSetChanged();
                pager.setCurrentItem(position);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.v("AndroBoum", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.addValueEventListener(postListener);
        pager.setAdapter(adapter);
    }

    class MyPagerAdapter extends PagerAdapter {
        List<Profil> liste;
        Context context;

        public MyPagerAdapter(Context context, List<Profil> liste) {
            this.liste = liste;
            this.context = context;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // on va chercher la layout
            ViewGroup layout = (ViewGroup) View.inflate(context, R.layout.other_user_fragment,null);
            // on l'ajoute à la vue
            container.addView(layout);
            // on le remplit en fonction du profil
            remplirLayout(layout, liste.get(position));
            // et on retourne ce layout
            return layout;
        }

        @Override
        public int getCount() {
            return liste.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        private void remplirLayout(ViewGroup layout, final Profil p) {
            ImageView profil_user = (ImageView) layout.findViewById(R.id.imageView);
            ImageView connect_user = (ImageView) layout.findViewById(R.id.imageView2);
            TextView mail_user = (TextView) layout.findViewById(R.id.emailAutre);
            TextView score_user = (TextView) layout.findViewById(R.id.score);
            // récupération de l'image du profil
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference photoRef = storage.getReference().child(p.getEmail() + "/photo.jpg");
            if (photoRef != null) {
                GlideApp.with(context).load(photoRef).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.ic_person_black_24dp).into(profil_user);
            }
            if (!p.isConnected()) {
                connect_user.setVisibility(View.GONE);
            }
            // récupération de l'email
            mail_user.setText(p.getEmail());
            Log.v("Androboum", "bingo" + p.getEmail());
            //récupération du score
            score_user.setText(String.valueOf(p.getScore()));
            //ajout de la possibilité de bomber l'individu
            Button bomber = (Button) layout.findViewById(R.id.button3);
            bomber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AndroBoumApp.getBomber().setBomb(p, new Bomber.BomberInterface() {
                        @Override
                        public void userBombed() {
                        }
                        @Override
                        public void userBomber() {
                            // on lance l'activité de contrôle de la bombe
                            Intent intent = new Intent(context, BombActivity.class);
                            context.startActivity(intent);

                        }
                    });
                }
            });

        }
    }
}
